package fr.techad.sonar;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DecoratorBarriers;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.PostJob;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;

import fr.techad.sonar.GerritConfiguration.GerritReviewConfiguration;
import fr.techad.sonar.GerritConfiguration.GerritServerConfiguration;
import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.ReviewInput;
import fr.techad.sonar.gerrit.ReviewUtils;

@DependsUpon(DecoratorBarriers.ISSUES_TRACKED)
public class GerritPostJob implements PostJob {
    private static final Logger LOG = LoggerFactory.getLogger(GerritPostJob.class);
    private static final String PROP_START = "${";
    private static final int PROP_START_LENGTH = PROP_START.length();
    private static final char PROP_END = '}';
    private final Settings settings;
    private GerritServerConfiguration gerritServerConfiguration = GerritConfiguration.serverConfiguration();
    private GerritReviewConfiguration gerritReviewConfiguration = GerritConfiguration.reviewConfiguration();
    private GerritFacade gerritFacade;
    private ReviewInput reviewInput = ReviewHolder.getReviewInput();

    public GerritPostJob(Settings settings) {
        this.settings = settings;

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
    public void executeOn(Project project, SensorContext context) {
        if (!gerritServerConfiguration.isEnabled()) {
            LOG.info("[GERRIT PLUGIN] Analysis has finished. Plugin is disabled. No actions taken.");
            return;
        }

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
}
