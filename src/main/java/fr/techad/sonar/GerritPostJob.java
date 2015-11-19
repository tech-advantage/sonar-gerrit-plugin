package fr.techad.sonar;

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

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.GerritFacadeFactory;
import fr.techad.sonar.gerrit.ReviewInput;
import fr.techad.sonar.gerrit.ReviewUtils;

@DependsUpon(DecoratorBarriers.ISSUES_TRACKED)
public class GerritPostJob implements PostJob {
    private static final Logger LOG = LoggerFactory.getLogger(GerritPostJob.class);
    private final Settings settings;
    private GerritFacade gerritFacade;
    private final GerritConfiguration gerritConfiguration;
    private ReviewInput reviewInput = ReviewHolder.getReviewInput();

    public GerritPostJob(Settings settings, GerritFacadeFactory gerritFacadeFactory, GerritConfiguration gerritConfiguration) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritPostJob");
        this.settings = settings;
        this.gerritFacade = gerritFacadeFactory.getFacade();
        this.gerritConfiguration = gerritConfiguration;
    }

    @Override
    public void executeOn(Project project, SensorContext context) {
        if (!gerritConfiguration.isEnabled()) {
            LOG.info("[GERRIT PLUGIN] PostJob : analysis has finished. Plugin is disabled. No actions taken.");
            return;
        }

        if (!gerritConfiguration.isValid()) {
            LOG.info("[GERRIT PLUGIN] Analysis has finished. Not sending results to Gerrit, because configuration is not valid.");
            return;
        }

        try {
            LOG.info("[GERRIT PLUGIN] Analysis has finished. Sending results to Gerrit.");
            reviewInput.setMessage(ReviewUtils.substituteProperties(gerritConfiguration.getMessage(), settings));

            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Define message : {}", reviewInput.getMessage());
                LOG.debug("[GERRIT PLUGIN] Number of comments : {}", reviewInput.size());
            }

            int maxLevel = ReviewUtils.maxLevel(reviewInput);
            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Configured threshold {}, max review level {}",
                        gerritConfiguration.getThreshold(), ReviewUtils.valueToThreshold(maxLevel));
            }

            if (ReviewUtils.isEmpty(reviewInput)
                    || maxLevel < ReviewUtils.thresholdToValue(gerritConfiguration.getThreshold())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[GERRIT PLUGIN] Vote +1 for the label : {}", gerritConfiguration.getLabel());
                }
                reviewInput.setLabelToPlusOne(gerritConfiguration.getLabel());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("[GERRIT PLUGIN] Vote -1 for the label : {}", gerritConfiguration.getLabel());
                }
                reviewInput.setLabelToMinusOne(gerritConfiguration.getLabel());
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Send review for ChangeId={}, RevisionId={}",
                        gerritConfiguration.getChangeId(), gerritConfiguration.getRevisionId());
            }

            gerritFacade.setReview(reviewInput);

        } catch (GerritPluginException e) {
            LOG.error("[GERRIT PLUGIN] Error sending review to Gerrit", e);
        }
    }

    @DependsUpon
    public String dependsOnViolations() {
        return DecoratorBarriers.ISSUES_ADDED;
    }

    @DependsUpon
    public Metric<?> dependsOnAlerts() {
        return CoreMetrics.ALERT_STATUS;
    }
}
