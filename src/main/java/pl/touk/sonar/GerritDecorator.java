package pl.touk.sonar;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.*;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.rules.Violation;
import pl.touk.sonar.gerrit.GerritFacade;
import pl.touk.sonar.gerrit.ReviewComment;
import pl.touk.sonar.gerrit.ReviewInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//http://sonarqube.15.x6.nabble.com/sonar-dev-Decorator-executed-a-lot-of-times-td5011536.html
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritDecorator implements Decorator, PostJob {
    private final static Logger LOG = LoggerFactory.getLogger(GerritDecorator.class);
    private static final String COMMENT_FORMAT = "[%s] Severity: %s, Message: %s";
    private GerritConfiguration gerritConfiguration;
    private GerritFacade gerritFacade;
    //Sonar's long name to Gerrit original file name map.
    private Map<String, String> gerritModifiedFiles;
    private ReviewInput reviewInput = new ReviewInput();

    public GerritDecorator(Settings settings) {
        this.gerritConfiguration = new GerritConfiguration();
        gerritConfiguration.setHost(settings.getString(PropertyKey.GERRIT_HOST));
        gerritConfiguration.setHttpPort(settings.getInt(PropertyKey.GERRIT_HTTP_PORT));
        gerritConfiguration.setHttpUsername(settings.getString(PropertyKey.GERRIT_HTTP_USERNAME));
        gerritConfiguration.setHttpPassword(settings.getString(PropertyKey.GERRIT_HTTP_PASSWORD));
        gerritConfiguration.setProjectName(settings.getString(PropertyKey.GERRIT_PROJECT));
        gerritConfiguration.setChangeId(settings.getString(PropertyKey.GERRIT_CHANGE_ID));
        gerritConfiguration.setRevisionId(settings.getString(PropertyKey.GERRIT_REVISION_ID));
        gerritConfiguration.assertGerritConfiguration();
        gerritFacade = new GerritFacade(gerritConfiguration.getHost(), gerritConfiguration.getHttpPort(), gerritConfiguration.getHttpUsername(), gerritConfiguration.getHttpPassword());
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
        LOG.info("Analysis has finished. Sending results to Gerrit.");
        try {
            reviewInput.setLabelToPlusOne();
            gerritFacade.setReview(gerritConfiguration.getChangeId(), gerritConfiguration.getRevisionId(), reviewInput);
        } catch (GerritPluginException e) {
            LOG.error("Error sending review to Gerrit", e);
        }
    }

    protected void processFileResource(@NotNull Resource resource, @NotNull DecoratorContext context) {
        if (gerritModifiedFiles.containsKey(resource.getLongName())) {
            LOG.info("File in Sonar {} matches file in Gerrit {}", resource.getLongName());
            List<ReviewComment> comments = new ArrayList<ReviewComment>();
            for (Violation violation : context.getViolations()) {
                LOG.info("Violation found: {}", violation.toString());
                comments.add(violationToComment(violation));
            }
            reviewInput.comments.put(gerritModifiedFiles.get(resource.getLongName()), comments);
        }
    }

    protected ReviewComment violationToComment(Violation violation) {
        ReviewComment result = new ReviewComment();
        result.line = violation.getLineId();
        result.message = String.format(COMMENT_FORMAT, StringUtils.capitalize(violation.getRule().getRepositoryKey()), violation.getSeverity().toString(), violation.getMessage());
        return result;
    }

    protected void assertOrFetchGerritModifiedFiles() throws GerritPluginException {
        if (gerritModifiedFiles != null) {
            return;
        }
        gerritModifiedFiles = gerritFacade.listFiles(gerritConfiguration.getChangeId(), gerritConfiguration.getRevisionId());
    }

    @DependsUpon
    public String dependsOnViolations() {
        return DecoratorBarriers.END_OF_VIOLATIONS_GENERATION;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return true;
    }


}
