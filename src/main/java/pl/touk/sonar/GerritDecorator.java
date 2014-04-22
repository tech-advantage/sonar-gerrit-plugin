package pl.touk.sonar;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.*;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasuresFilters;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.rules.Violation;
import pl.touk.sonar.gerrit.GerritFacade;
import pl.touk.sonar.gerrit.ReviewFileComment;
import pl.touk.sonar.gerrit.ReviewInput;
import pl.touk.sonar.gerrit.ReviewLineComment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//http://sonarqube.15.x6.nabble.com/sonar-dev-Decorator-executed-a-lot-of-times-td5011536.html
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritDecorator implements Decorator, PostJob {
    private final static Logger LOG = LoggerFactory.getLogger(GerritDecorator.class);
    private static final String ISSUE_FORMAT = "[%s] Severity: %s, Message: %s";
    private static final String ALERT_FORMAT = "[ALERT] Severity: %s, Message: %s";
    private GerritConfiguration gerritConfiguration;
    private GerritFacade gerritFacade;
    //Sonar's long name to Gerrit original file name map.
    private Map<String, String> gerritModifiedFiles;
    private ReviewInput reviewInput = new ReviewInput();

    public GerritDecorator(Settings settings) {
        this.gerritConfiguration = new GerritConfiguration();
        gerritConfiguration.setScheme(settings.getString(PropertyKey.GERRIT_SCHEME));
        gerritConfiguration.setHost(settings.getString(PropertyKey.GERRIT_HOST));
        gerritConfiguration.setHttpPort(settings.getInt(PropertyKey.GERRIT_HTTP_PORT));
        gerritConfiguration.setHttpUsername(settings.getString(PropertyKey.GERRIT_HTTP_USERNAME));
        gerritConfiguration.setHttpPassword(settings.getString(PropertyKey.GERRIT_HTTP_PASSWORD));
        gerritConfiguration.setBaseUrl(settings.getString(PropertyKey.GERRIT_BASE_URL));
        gerritConfiguration.setProjectName(settings.getString(PropertyKey.GERRIT_PROJECT));
        gerritConfiguration.setChangeId(settings.getString(PropertyKey.GERRIT_CHANGE_ID));
        gerritConfiguration.setRevisionId(settings.getString(PropertyKey.GERRIT_REVISION_ID));
        gerritConfiguration.assertGerritConfiguration();
    }

    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        if (!ResourceUtils.isFile(resource)) {
            return;
        }
        if (!gerritConfiguration.isValid()) {
            return;
        }
        try {
            assertGerritFacade();
            assertOrFetchGerritModifiedFiles();
            processFileResource(resource, context);
        } catch (GerritPluginException e) {
            LOG.error("Error processing Gerrit Plugin decorator", e);
        }
    }

    @Override
    public void executeOn(Project project, SensorContext context) {
        if (!gerritConfiguration.isValid()) {
            LOG.info("Analysis has finished. Not sending results to Gerrit, because configuration is not valid.");
            return;
        }
        try {
            LOG.info("Analysis has finished. Sending results to Gerrit.");
            assertGerritFacade();
            reviewInput.setLabelToPlusOne();
            gerritFacade.setReview(gerritConfiguration.getChangeId(), gerritConfiguration.getRevisionId(), reviewInput);
        } catch (GerritPluginException e) {
            LOG.error("Error sending review to Gerrit", e);
        }
    }

    protected void processFileResource(@NotNull Resource resource, @NotNull DecoratorContext context) {
        if (gerritModifiedFiles.containsKey(resource.getLongName())) {
            LOG.info("File in Sonar {} matches file in Gerrit {}", resource.getLongName(), gerritModifiedFiles.get(resource.getLongName()));
            List<ReviewFileComment> comments = new ArrayList<ReviewFileComment>();
            commentViolations(context, comments);
            commentAlerts(context, comments);
            if (!comments.isEmpty()) {
                reviewInput.comments.put(gerritModifiedFiles.get(resource.getLongName()), comments);
            }
        }
    }

    private void commentViolations(DecoratorContext context, List<ReviewFileComment> comments) {
        for (Violation violation : context.getViolations()) {
            LOG.info("Violation found: {}", violation.toString());
            comments.add(violationToComment(violation));
        }
    }

    /**
     * This is usable with sonar-file-alert plugin.
     */
    private void commentAlerts(DecoratorContext context, List<ReviewFileComment> comments) {
        for (Measure measure : context.getMeasures(MeasuresFilters.all())) {
            if (measure.getAlertStatus() == null || measure.getAlertStatus() == Metric.Level.OK) {
                continue;
            }
            comments.add(measureToComment(measure));
        }
    }

    protected ReviewLineComment violationToComment(Violation violation) {
        ReviewLineComment result = new ReviewLineComment();
        result.line = violation.getLineId();
        result.message = String.format(ISSUE_FORMAT, StringUtils.capitalize(violation.getRule().getRepositoryKey()), violation.getSeverity().toString(), violation.getMessage());
        return result;
    }

    protected ReviewFileComment measureToComment(Measure measure) {
        ReviewFileComment result = new ReviewFileComment();
        result.message = String.format(ALERT_FORMAT, measure.getAlertStatus().toString(), measure.getAlertText());
        return result;
    }

    protected void assertGerritFacade() {
        assert(gerritConfiguration.isValid());
        if (gerritFacade == null) {
            gerritFacade = new GerritFacade(gerritConfiguration.getScheme(), gerritConfiguration.getHost(), gerritConfiguration.getHttpPort(),
                    gerritConfiguration.getHttpUsername(), gerritConfiguration.getHttpPassword(), gerritConfiguration.getBaseUrl());
        }
    }


    protected void assertOrFetchGerritModifiedFiles() throws GerritPluginException {
        if (gerritModifiedFiles != null) {
            return;
        }
        gerritModifiedFiles = gerritFacade.listFiles(gerritConfiguration.getChangeId(), gerritConfiguration.getRevisionId());
    }

    @DependsUpon
    public String dependsOnViolations() {
        return DecoratorBarriers.ISSUES_ADDED;
    }

    @DependsUpon
    public Metric dependsOnAlerts() {
        return CoreMetrics.ALERT_STATUS;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return true;
    }


}
