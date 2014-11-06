package fr.techad.sonar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorBarriers;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.PostJob;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasuresFilters;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.Level;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;

import fr.techad.sonar.GerritConfiguration.GerritReviewConfiguration;
import fr.techad.sonar.GerritConfiguration.GerritServerConfiguration;
import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.ReviewFileComment;
import fr.techad.sonar.gerrit.ReviewInput;
import fr.techad.sonar.gerrit.ReviewLineComment;
import fr.techad.sonar.gerrit.ReviewUtils;

@DependsUpon(DecoratorBarriers.ISSUES_TRACKED)
public class GerritDecorator implements Decorator, PostJob {
    private static final Logger LOG = LoggerFactory.getLogger(GerritDecorator.class);
    private static final String ISSUE_FORMAT = "[%s] New: %s Severity: %s, Message: %s";
    private static final String ALERT_FORMAT = "[ALERT] Severity: %s, Message: %s";
    private static final String PROP_START = "${";
    private static final int PROP_START_LENGTH = PROP_START.length();
    private static final char PROP_END = '}';
    private final Settings settings;
    private GerritServerConfiguration gerritServerConfiguration = GerritConfiguration.serverConfiguration();
    private GerritReviewConfiguration gerritReviewConfiguration = GerritConfiguration.reviewConfiguration();
    private GerritFacade gerritFacade;
    // Sonar's long name to Gerrit original file name map.
    private Map<String, String> gerritModifiedFiles;
    private ReviewInput reviewInput = new ReviewInput();
    private final ResourcePerspectives perspectives;

    public GerritDecorator(Settings settings, ResourcePerspectives perspectives) {
        this.settings = settings;
        this.perspectives = perspectives;

        gerritServerConfiguration.enable(settings.getBoolean(PropertyKey.GERRIT_ENABLED))
                .setScheme(settings.getString(PropertyKey.GERRIT_SCHEME))
                .setHost(settings.getString(PropertyKey.GERRIT_HOST))
                .setHttpPort(settings.getInt(PropertyKey.GERRIT_HTTP_PORT))
                .setHttpUsername(settings.getString(PropertyKey.GERRIT_HTTP_USERNAME))
                .setHttpPassword(settings.getString(PropertyKey.GERRIT_HTTP_PASSWORD))
                .setHttpAuthScheme(settings.getString(PropertyKey.GERRIT_HTTP_AUTH_SCHEME))
                .setBasePath(settings.getString(PropertyKey.GERRIT_BASE_PATH));
        gerritServerConfiguration.assertGerritServerConfiguration();

        gerritReviewConfiguration.setLabel(settings.getString(PropertyKey.GERRIT_LABEL))
                .setMessage(settings.getString(PropertyKey.GERRIT_MESSAGE))
                .setThreshold(settings.getString(PropertyKey.GERRIT_THRESHOLD))
                .setProjectName(settings.getString(PropertyKey.GERRIT_PROJECT))
                .setBranchName(settings.getString(PropertyKey.GERRIT_BRANCH))
                .setChangeId(settings.getString(PropertyKey.GERRIT_CHANGE_ID))
                .setRevisionId(settings.getString(PropertyKey.GERRIT_REVISION_ID));
        gerritReviewConfiguration.assertGerritReviewConfiguration();
    }

    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        Issuable issuable = perspectives.as(Issuable.class, resource);
        if (issuable == null) {
            LOG.warn("[GERRIT PLUGIN] No issuable found");
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Decorate: {}", resource.getLongName());
            }
            if (!ResourceUtils.isFile(resource)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[GERRIT PLUGIN] {} is not a file", resource.getLongName());
                }
                return;
            }
            if (!GerritConfiguration.isValid()) {
                LOG.debug("[GERRIT PLUGIN] Configuration is not valid");
                return;
            }

            try {
                LOG.debug("[GERRIT PLUGIN] Start Sonar decoration for Gerrit");
                assertGerritFacade();
                assertOrFetchGerritModifiedFiles();
            } catch (GerritPluginException e) {
                LOG.error("[GERRIT PLUGIN] Error getting Gerrit datas", e);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                        "[GERRIT PLUGIN] Look for in Gerrit if the file was under review, name={}, key={}, effective key={}",
                        resource.getLongName(), resource.getKey(), resource.getEffectiveKey());
            }
            if (gerritModifiedFiles.containsKey(resource.getLongName())) {
                LOG.info("[GERRIT PLUGIN] File in Sonar {} matches file in Gerrit {}", resource.getLongName(),
                        gerritModifiedFiles.get(resource.getLongName()));
                processFileResource(resource, context, issuable);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[GERRIT PLUGIN] File {} is not under review", resource.getLongName());
                }
            }
        }
    }

    @Override
    public void executeOn(Project project, SensorContext context) {
        if (!GerritConfiguration.isValid()) {
            LOG.info("[GERRIT PLUGIN] Analysis has finished. Not sending results to Gerrit, because configuration is not valid.");
            return;
        }
        try {
            LOG.info("[GERRIT PLUGIN] Analysis has finished. Sending results to Gerrit.");
            assertGerritFacade();
            reviewInput.setMessage(substituteProperties(gerritReviewConfiguration.getMessage()));

            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Define message : {}", reviewInput.getMessage());
                LOG.debug("[GERRIT PLUGIN] Number of comments : {}", reviewInput.size());
            }

            int maxLevel = ReviewUtils.maxLevel(reviewInput);
            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Configured threshold {}, max review level {}",
                        gerritReviewConfiguration.getThreshold(), ReviewUtils.valueToThreshold(maxLevel));
            }

            if (ReviewUtils.isEmpty(reviewInput)
                    || maxLevel < ReviewUtils.thresholdToValue(gerritReviewConfiguration.getThreshold())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[GERRIT PLUGIN] Vote +1 for the label : {}", gerritReviewConfiguration.getLabel());
                }
                reviewInput.setLabelToPlusOne(gerritReviewConfiguration.getLabel());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[GERRIT PLUGIN] Vote -1 for the label : {}", gerritReviewConfiguration.getLabel());
                }
                reviewInput.setLabelToMinusOne(gerritReviewConfiguration.getLabel());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Send review for ChangeId={}, RevisionId={}",
                        gerritReviewConfiguration.getChangeId(), gerritReviewConfiguration.getRevisionId());
            }

            gerritFacade.setReview(gerritReviewConfiguration.getProjectName(),
                    gerritReviewConfiguration.getBranchName(), gerritReviewConfiguration.getChangeId(),
                    gerritReviewConfiguration.getRevisionId(), reviewInput);

        } catch (GerritPluginException e) {
            LOG.error("[GERRIT PLUGIN] Error sending review to Gerrit", e);
        }
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean enabled = gerritServerConfiguration.isEnabled();
        LOG.info("[GERRIT PLUGIN] Will{}execute plugin on project \'{}\'.", enabled ? " " : " NOT ", project.getName());
        return enabled;
    }

    @DependsUpon
    public String dependsOnViolations() {
        return DecoratorBarriers.ISSUES_ADDED;
    }

    @DependsUpon
    public Metric dependsOnAlerts() {
        return CoreMetrics.ALERT_STATUS;
    }

    protected void assertGerritFacade() {
        assert gerritServerConfiguration.isValid();
        if (gerritFacade == null) {
            gerritFacade = new GerritFacade(gerritServerConfiguration.getScheme(), gerritServerConfiguration.getHost(),
                    gerritServerConfiguration.getHttpPort(), gerritServerConfiguration.getHttpUsername(),
                    gerritServerConfiguration.getHttpPassword(), gerritServerConfiguration.getBasePath(),
                    gerritServerConfiguration.getHttpAuthScheme());
        }
    }

    protected void assertOrFetchGerritModifiedFiles() throws GerritPluginException {
        if (gerritModifiedFiles != null) {
            return;
        }
        gerritModifiedFiles = gerritFacade.listFiles(gerritReviewConfiguration.getProjectName(),
                gerritReviewConfiguration.getBranchName(), gerritReviewConfiguration.getChangeId(),
                gerritReviewConfiguration.getRevisionId());
        if (LOG.isDebugEnabled()) {
            LOG.debug("[GERRIT PLUGIN] Modified files in gerrit (keys) : {}", gerritModifiedFiles.keySet());
            LOG.debug("[GERRIT PLUGIN] Modified files in gerrit (values): {}", gerritModifiedFiles.values());
        }
    }

    protected ReviewLineComment issueToComment(Issue issue) {
        ReviewLineComment result = new ReviewLineComment();

        result.setLine(issue.line());
        result.setMessage(String.format(ISSUE_FORMAT, issue.isNew(),
                StringUtils.capitalize(issue.ruleKey().toString()), issue.severity(), issue.message()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("[GERRIT PLUGIN] issueToComment {}", result.toString());
        }
        return result;
    }

    protected ReviewFileComment measureToComment(Measure measure) {
        ReviewFileComment result = new ReviewFileComment();
        result.setMessage(String.format(ALERT_FORMAT, measure.getAlertStatus().toString(), measure.getAlertText()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("[GERRIT PLUGIN] measureToComment {}", result.toString());
        }

        return result;
    }

    protected void processFileResource(@NotNull Resource resource, @NotNull DecoratorContext context, Issuable issuable) {
        List<ReviewFileComment> comments = new ArrayList<ReviewFileComment>();
        commentIssues(issuable, comments);
        commentAlerts(context, comments);
        if (!comments.isEmpty()) {
            reviewInput.addComments(gerritModifiedFiles.get(resource.getLongName()), comments);
        }
    }

    protected String substituteProperties(String originalMessage) {
        String subtitutedString = originalMessage;

        if (StringUtils.contains(originalMessage, PROP_START)) {
            List<String> prop = new ArrayList<String>();
            String tempString = originalMessage;

            while (StringUtils.contains(tempString, PROP_START)) {
                tempString = tempString.substring(tempString.indexOf(PROP_START));
                prop.add(tempString.substring(PROP_START_LENGTH, tempString.indexOf(PROP_END)));
                tempString = StringUtils.substring(tempString, tempString.indexOf(PROP_END));
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Found {} properties to replace ({})", prop.size(), prop.toString());
            }

            for (String p : prop) {
                subtitutedString = StringUtils.replace(subtitutedString, PROP_START + p + PROP_END,
                        settings.getString(p));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] New message is {}.", subtitutedString);
            }
        } else {
            LOG.debug("[GERRIT PLUGIN] No message subtitution to do.");
        }

        return subtitutedString;
    }

    private void commentIssues(Issuable issuable, List<ReviewFileComment> comments) {
        List<Issue> issues = issuable.issues();
        if (LOG.isDebugEnabled()) {
            LOG.debug("[GERRIT PLUGIN] Found {} issues", issues.size());
        }
        for (Issue issue : issues) {
            LOG.info("[GERRIT PLUGIN] Issue found: {}", issue.toString());
            if (StringUtils.equals(issue.resolution(), Issue.RESOLUTION_FALSE_POSITIVE)) {
                LOG.info("[GERRIT PLUGIN] Issue marked as false-positive. Will not push back to Gerrit.");
                continue;
            }
            comments.add(issueToComment(issue));
        }
    }

    /**
     * This is usable with sonar-file-alert plugin.
     */
    private void commentAlerts(DecoratorContext context, List<ReviewFileComment> comments) {
        LOG.debug("[GERRIT PLUGIN] Found {} alerts", context.getMeasures(MeasuresFilters.all()).size());
        for (Measure measure : context.getMeasures(MeasuresFilters.all())) {
            Level level = measure.getAlertStatus();
            if (level == null || level == Metric.Level.OK) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[GERRIT PLUGIN] Alert level is {}. Continue.", level);
                }
                continue;
            }
            LOG.info("[GERRIT PLUGIN] Alert found: {}", level.toString());
            comments.add(measureToComment(measure));
        }
    }
}
