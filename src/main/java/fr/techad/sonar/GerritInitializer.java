package fr.techad.sonar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Initializer;
import org.sonar.api.resources.Project;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.gerrit.GerritFacade;

public class GerritInitializer extends Initializer {
    private static final Logger LOG = LoggerFactory.getLogger(GerritInitializer.class);
    private final GerritConfiguration gerritConfiguration;
    private GerritFacade gerritFacade;

    public GerritInitializer(GerritConfiguration gerritConfiguration, GerritFacade gerritFacade) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritInitializer");
        this.gerritConfiguration = gerritConfiguration;
        this.gerritFacade = gerritFacade;
    }

    @Override
    public void execute(Project project) {
        try {
            gerritFacade.listFiles();
        } catch (GerritPluginException e) {
            LOG.error("[GERRIT PLUGIN] Error getting Gerrit datas", e);
        }
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean enabled = gerritConfiguration.isEnabled();
        LOG.info("[GERRIT PLUGIN] Initializer : will{}execute plugin on project \'{}\'.", enabled ? " " : " NOT ",
                project.getName());
        return enabled;
    }
}
