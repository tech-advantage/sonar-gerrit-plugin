package pl.touk.sonar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorBarriers;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;

//http://sonarqube.15.x6.nabble.com/sonar-dev-Decorator-executed-a-lot-of-times-td5011536.html
//@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritDecorator implements Decorator {
    private final static Logger LOG = LoggerFactory.getLogger(GerritDecorator.class);
    private Settings settings;

    public GerritDecorator(Settings settings) {
        this.settings = settings;
    }


    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        LOG.info("Decorate on resource {}", resource);
        if (ResourceUtils.isRootProject(resource)) {
            decorateProject((Project)resource, context);
        }
    }

    protected void decorateProject(Project project, DecoratorContext context) {
        Review review = new Review(project, context);
        review.setGerritHost(settings.getString(PropertyKey.GERRIT_HOST));
        review.setGerritPort(settings.getInt(PropertyKey.GERRIT_PORT));
        review.setGerritUsername(settings.getString(PropertyKey.GERRIT_USERNAME));
        review.setGerritPassword(settings.getString(PropertyKey.GERRIT_PASSWORD));
        review.setGerritProjectName(settings.getString(PropertyKey.GERRIT_PROJECT));
        review.setGerritChangeId(settings.getString(PropertyKey.GERRIT_CHANGE_ID));
        review.setGerritRevisionId(settings.getString(PropertyKey.GERRIT_REVISION_ID));
        new ProjectProcessor(review).process();

    }

    @DependsUpon
    public String dependsOnViolations() {
        return DecoratorBarriers.END_OF_VIOLATIONS_GENERATION;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return true;
    }

    private boolean shouldDecorateResource(final Resource resource) {
        return ResourceUtils.isRootProject(resource);
    }
}
