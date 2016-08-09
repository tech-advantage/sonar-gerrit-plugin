package fr.techad.sonar.gerrit.factory;

import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import fr.techad.sonar.gerrit.GerritConnector;
import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.network.rest.GerritRestConnector;
import fr.techad.sonar.gerrit.network.rest.GerritRestFacade;
import fr.techad.sonar.gerrit.network.ssh.GerritSshConnector;
import fr.techad.sonar.gerrit.network.ssh.GerritSshFacade;

@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritFacadeFactory {
    private static final Logger LOG = Loggers.get(GerritFacadeFactory.class);

    GerritConnector gerritConnector;
    GerritFacade gerritFacade;

    public GerritFacadeFactory(GerritConnectorFactory gerritConnectorFactory) {
        this.gerritConnector = gerritConnectorFactory.getConnector();
        if (gerritConnector instanceof GerritRestConnector) {
            LOG.debug("[GERRIT PLUGIN] Using REST connector.");
            gerritFacade = new GerritRestFacade(gerritConnectorFactory);
        } else if (gerritConnector instanceof GerritSshConnector) {
            LOG.debug("[GERRIT PLUGIN] Using SSH facade.");
            gerritFacade = new GerritSshFacade(gerritConnectorFactory);
        } else {
            LOG.error("[GERRIT PLUGIN] Unknown type of connector. Cannot assign facade.");
        }
    }

    public GerritFacade getFacade() {
        return gerritFacade;
    }
}
