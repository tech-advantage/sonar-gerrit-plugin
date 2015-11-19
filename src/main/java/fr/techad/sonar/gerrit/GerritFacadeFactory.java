package fr.techad.sonar.gerrit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchComponent;
import org.sonar.api.batch.InstantiationStrategy;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritFacadeFactory implements BatchComponent {
	private static final Logger LOG = LoggerFactory.getLogger(GerritFacadeFactory.class);

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
