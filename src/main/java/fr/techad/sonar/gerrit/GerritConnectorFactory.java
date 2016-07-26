package fr.techad.sonar.gerrit;

import org.sonar.api.batch.BatchSide;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritConstants;

@BatchSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritConnectorFactory {
    private static final Logger LOG = Loggers.get(GerritConnectorFactory.class);

    GerritConfiguration gerritConfiguration;
    GerritConnector gerritConnector;

    public GerritConnectorFactory(GerritConfiguration gerritConfiguration) {
        this.gerritConfiguration = gerritConfiguration;

        if (gerritConfiguration.getScheme().startsWith(GerritConstants.SCHEME_HTTP)) {
            LOG.debug("[GERRIT PLUGIN] Using REST connector.");
            gerritConnector = new GerritRestConnector(gerritConfiguration);
        } else if (gerritConfiguration.getScheme().equals(GerritConstants.SCHEME_SSH)) {
            LOG.debug("[GERRIT PLUGIN] Using SSH connector.");
            gerritConnector = new GerritSshConnector(gerritConfiguration);
        } else {
            LOG.error("[GERRIT PLUGIN] Unknown scheme.");
        }
    }

    public GerritConnector getConnector() {
        return gerritConnector;
    }
}
