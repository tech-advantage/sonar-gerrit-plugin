package fr.techad.sonar;

import org.sonar.api.batch.Initializer;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import fr.techad.sonar.gerrit.GerritFacade;

public class GerritInitializer extends Initializer {
    private static final Logger LOG = Loggers.get(GerritInitializer.class);
    private final GerritConfiguration gerritConfiguration;
    private GerritFacade gerritFacade;

    public GerritInitializer(GerritConfiguration gerritConfiguration, GerritFacade gerritFacade) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritInitializer");
        this.gerritConfiguration = gerritConfiguration;
        this.gerritFacade = gerritFacade;
    }

    @Override
    public void execute(Project project) {
        if (gerritConfiguration.forceBranch()) {
            LOG.debug("[GERRIT PLUGIN] Force project branch to {}", gerritConfiguration.getBranchName());
            project.setBranch(gerritConfiguration.getBranchName());
        }
        
        try {
            gerritFacade.listFiles();
        } catch (GerritPluginException e) {
            LOG.error("[GERRIT PLUGIN] Error getting Gerrit datas", e);
        }
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean willRun = gerritConfiguration.isEnabled();
        
        LOG.info("[GERRIT PLUGIN] Initializer : will{}execute plugin on project \'{}\'.", willRun ? " " : " NOT ",
                project.getName());

        if (!gerritConfiguration.isValid()) {
            LOG.info("[GERRIT PLUGIN] Initializer : Configuration is not valid, will not execute.");
            willRun = false;
        }

        return willRun;
    }
}
