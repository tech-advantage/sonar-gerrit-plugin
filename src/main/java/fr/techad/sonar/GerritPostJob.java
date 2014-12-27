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
    private GerritFacade gerritFacade;
    private final GerritConfiguration gerritConfiguration;
    private ReviewInput reviewInput = ReviewHolder.getReviewInput();

    public GerritPostJob(Settings settings, GerritFacade gerritFacade, GerritConfiguration gerritConfiguration) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritPostJob");
        this.settings = settings;
        this.gerritFacade = gerritFacade;
        this.gerritConfiguration = gerritConfiguration;
    }

    @Override
    public void executeOn(Project project, SensorContext context) {
        if (!gerritConfiguration.isEnabled()) {
            LOG.info("[GERRIT PLUGIN] PostJob : analysis has finished. Plugin is disabled. No actions taken.");
            return;
        }

        try {
            LOG.info("[GERRIT PLUGIN] Analysis has finished. Sending results to Gerrit.");
            reviewInput.setMessage(substituteProperties(gerritConfiguration.getMessage()));

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
