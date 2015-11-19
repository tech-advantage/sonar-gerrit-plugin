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
import org.sonar.api.component.ResourcePerspectives;
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

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.GerritFacadeFactory;
import fr.techad.sonar.gerrit.ReviewFileComment;
import fr.techad.sonar.gerrit.ReviewInput;
import fr.techad.sonar.gerrit.ReviewLineComment;

@DependsUpon(DecoratorBarriers.ISSUES_TRACKED)
public class GerritDecorator implements Decorator {
    private static final Logger LOG = LoggerFactory.getLogger(GerritDecorator.class);
    private static final String ISSUE_FORMAT = "[%s] New: %s Severity: %s, Message: %s";
    private static final String ALERT_FORMAT = "[ALERT] Severity: %s, Message: %s";
    private Map<String, String> gerritModifiedFiles;
    private ReviewInput reviewInput = ReviewHolder.getReviewInput();
    private final GerritConfiguration gerritConfiguration;
    private final GerritFacade gerritFacade;
    private final ResourcePerspectives perspectives;

    public GerritDecorator(ResourcePerspectives perspectives, GerritFacadeFactory gerritFacadeFactory,
            GerritConfiguration gerritConfiguration) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritDecorator");
        this.perspectives = perspectives;
        this.gerritFacade = gerritFacadeFactory.getFacade();
        this.gerritConfiguration = gerritConfiguration;
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
            if (!gerritConfiguration.isValid()) {
                LOG.debug("[GERRIT PLUGIN] Configuration is not valid");
                return;
            }

            try {
                LOG.debug("[GERRIT PLUGIN] Start Sonar decoration for Gerrit");
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
    public boolean shouldExecuteOnProject(Project project) {
        boolean enabled = gerritConfiguration.isEnabled();
        LOG.info("[GERRIT PLUGIN] Decorator : will{}execute plugin on project \'{}\'.", enabled ? " " : " NOT ",
                project.getName());
        return enabled;
    }

    @DependsUpon
    public String dependsOnViolations() {
        return DecoratorBarriers.ISSUES_ADDED;
    }

    @DependsUpon
    public Metric<?> dependsOnAlerts() {
        return CoreMetrics.ALERT_STATUS;
    }

    protected void assertOrFetchGerritModifiedFiles() throws GerritPluginException {
        if (gerritModifiedFiles != null) {
            return;
        }
        gerritModifiedFiles = gerritFacade.listFiles();
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

    protected ReviewFileComment measureToComment(Measure<?> measure) {
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

    private void commentAlerts(DecoratorContext context, List<ReviewFileComment> comments) {
        LOG.debug("[GERRIT PLUGIN] Found {} alerts", context.getMeasures(MeasuresFilters.all()).size());
        for (Measure<?> measure : context.getMeasures(MeasuresFilters.all())) {
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

    @Override
    public String toString() {
        return "GerritDecorator";
    }
}
