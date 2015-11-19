package fr.techad.sonar.gerrit;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import fr.techad.sonar.GerritPluginException;

public class GerritSshFacade extends GerritFacade {
	private static final Logger LOG = LoggerFactory.getLogger(GerritSshFacade.class);
	private static final String COMMIT_MSG = "/COMMIT_MSG";
	private static final String ERROR_LISTING = "Error listing files";
	private static final String ERROR_SETTING = "Error setting review";

	private final GerritConnector gerritConnector;
	private Map<String, String> gerritFileList = new HashMap<String, String>();

	public GerritSshFacade(GerritConnectorFactory gerritConnectorFactory) {
		LOG.debug("[GERRIT PLUGIN] Instanciating GerritRestFacade");
		this.gerritConnector = gerritConnectorFactory.getConnector();
	}

	@NotNull
	public Map<String, String> listFiles() throws GerritPluginException {
		if (!gerritFileList.isEmpty()) {
			LOG.debug("[GERRIT PLUGIN] File list already filled. Not calling Gerrit.");
		} else {
			try {
				String rawJsonString = gerritConnector.listFiles();
				JsonArray files = new JsonParser().parse(rawJsonString.split("\r?\n")[0]).getAsJsonObject()
						.getAsJsonObject("currentPatchSet").getAsJsonArray("files");

				for (JsonElement jsonElement : files) {
					String fileName = jsonElement.getAsJsonObject().get("file").getAsString();
					if (COMMIT_MSG.equals(fileName)) {
						continue;
					}
					gerritFileList.put(parseFileName(fileName), fileName);
				}
			} catch (IOException e) {
				throw new GerritPluginException(ERROR_LISTING, e);
			}
		}
		return Collections.unmodifiableMap(gerritFileList);
	}

	@Override
	public void setReview(@NotNull ReviewInput reviewInput) throws GerritPluginException {
		try {
			gerritConnector.setReview(formatReview(reviewInput));
		} catch (IOException e) {
			throw new GerritPluginException(ERROR_SETTING, e);
		}
	}
}
