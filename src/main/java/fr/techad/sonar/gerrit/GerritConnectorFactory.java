package fr.techad.sonar.gerrit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.BatchComponent;
import org.sonar.api.batch.InstantiationStrategy;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritConstants;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritConnectorFactory implements BatchComponent {
	private static final Logger LOG = LoggerFactory.getLogger(GerritConnectorFactory.class);

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
