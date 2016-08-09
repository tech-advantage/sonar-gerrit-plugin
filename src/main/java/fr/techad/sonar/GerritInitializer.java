package fr.techad.sonar;

import org.sonar.api.batch.Initializer;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.factory.GerritFacadeFactory;

public class GerritInitializer extends Initializer {
    private static final Logger LOG = Loggers.get(GerritInitializer.class);
    private final GerritConfiguration gerritConfiguration;
    private GerritFacade gerritFacade;

    public GerritInitializer(GerritConfiguration gerritConfiguration, GerritFacadeFactory gerritFacadeFactory) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritInitializer");
        this.gerritConfiguration = gerritConfiguration;
        this.gerritFacade = gerritFacadeFactory.getFacade();
    }

    @Override
    public void execute() {
        if (!gerritConfiguration.isEnabled()) {
            LOG.info("[GERRIT PLUGIN] Initializer : will NOT execute plugin.");
            return;
        }

        if (!gerritConfiguration.isValid()) {
            LOG.info("[GERRIT PLUGIN] Initializer : Configuration is not valid, will not execute.");
            return;
        }

        try {
            gerritFacade.listFiles();
        } catch (GerritPluginException e) {
            LOG.error("[GERRIT PLUGIN] Error getting Gerrit datas", e);
        }
    }
}
