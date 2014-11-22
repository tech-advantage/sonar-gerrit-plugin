package fr.techad.sonar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Initializer;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

import fr.techad.sonar.GerritConfiguration.GerritReviewConfiguration;
import fr.techad.sonar.GerritConfiguration.GerritServerConfiguration;

public class GerritInitializer extends Initializer {
    private static final Logger LOG = LoggerFactory.getLogger(GerritInitializer.class);
    private GerritServerConfiguration gerritServerConfiguration = GerritConfiguration.serverConfiguration();
    private GerritReviewConfiguration gerritReviewConfiguration = GerritConfiguration.reviewConfiguration();
    private final Settings settings;

    public GerritInitializer(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void execute(Project project) {
        LOG.debug("[GERRIT PLUGIN] Initializing server configuration ...");
        gerritServerConfiguration.enable(settings.getBoolean(PropertyKey.GERRIT_ENABLED))
                .setScheme(settings.getString(PropertyKey.GERRIT_SCHEME))
                .setHost(settings.getString(PropertyKey.GERRIT_HOST))
                .setHttpPort(settings.getInt(PropertyKey.GERRIT_HTTP_PORT))
                .setHttpUsername(settings.getString(PropertyKey.GERRIT_HTTP_USERNAME))
                .setHttpPassword(settings.getString(PropertyKey.GERRIT_HTTP_PASSWORD))
                .setHttpAuthScheme(settings.getString(PropertyKey.GERRIT_HTTP_AUTH_SCHEME))
                .setBasePath(settings.getString(PropertyKey.GERRIT_BASE_PATH));
        gerritServerConfiguration.assertGerritServerConfiguration();
        LOG.debug("[GERRIT PLUGIN] server configuration initialized !");

        LOG.debug("[GERRIT PLUGIN] Initializing review configuration ...");
        gerritReviewConfiguration.setLabel(settings.getString(PropertyKey.GERRIT_LABEL))
                .setMessage(settings.getString(PropertyKey.GERRIT_MESSAGE))
                .setThreshold(settings.getString(PropertyKey.GERRIT_THRESHOLD))
                .setProjectName(settings.getString(PropertyKey.GERRIT_PROJECT))
                .setBranchName(settings.getString(PropertyKey.GERRIT_BRANCH))
                .setChangeId(settings.getString(PropertyKey.GERRIT_CHANGE_ID))
                .setRevisionId(settings.getString(PropertyKey.GERRIT_REVISION_ID));
        gerritReviewConfiguration.assertGerritReviewConfiguration();
        LOG.debug("[GERRIT PLUGIN] review configuration initialized !");
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean enabled = settings.getBoolean(PropertyKey.GERRIT_ENABLED);
        LOG.info("[GERRIT PLUGIN] Initializer : will{}execute plugin on project \'{}\'.", enabled ? " " : " NOT ",
                project.getName());
        return enabled;
    }
}
