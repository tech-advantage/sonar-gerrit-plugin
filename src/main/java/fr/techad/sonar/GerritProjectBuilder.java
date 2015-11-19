package fr.techad.sonar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.bootstrap.ProjectBuilder;

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.GerritFacadeFactory;
import fr.techad.sonar.gerrit.ReviewInput;

public class GerritProjectBuilder extends ProjectBuilder {
	private static final Logger LOG = LoggerFactory.getLogger(GerritProjectBuilder.class);
	private final GerritConfiguration gerritConfiguration;
	private final GerritFacade gerritFacade;

	public GerritProjectBuilder(GerritConfiguration gerritConfiguration, GerritFacadeFactory gerritFacadeFactory) {
		LOG.debug("[GERRIT PLUGIN] Instanciating GerritProjectBuilder");
		this.gerritConfiguration = gerritConfiguration;
		this.gerritFacade = gerritFacadeFactory.getFacade();
	}

	@Override
	public void build(Context context) {
		if (!gerritConfiguration.isEnabled()) {
			LOG.error("[GERRIT PLUGIN] Plugin is disabled. Wont send.");
			return;
		}

		if (!gerritConfiguration.isValid()) {
			LOG.error("[GERRIT PLUGIN] Configuration is invalid. Wont send.");
			return;
		}

		ReviewInput ri = new ReviewInput();
		ri.setValueAndLabel(0, gerritConfiguration.getLabel());
		ri.setMessage("Sonar review in progress …");

		try {
			gerritFacade.setReview(ri);
		} catch (GerritPluginException e) {
			LOG.error("[GERRIT PLUGIN] Sending initial status failed", e);
		}
	}
}