package fr.techad.sonar.gerrit.network.rest;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.GerritConnector;
import fr.techad.sonar.gerrit.GerritFacade;

public class GerritRestFacade extends GerritFacade {
    private static final Logger LOG = Loggers.get(GerritRestFacade.class);
    private static final String JSON_RESPONSE_PREFIX = ")]}'";
    private static final String ERROR_LISTING = "Error listing files";

    public GerritRestFacade(GerritConnector gerritConnector) {
        super(gerritConnector);
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritRestFacade");
    }

    @Override
    protected void fillListFilesFomGerrit() throws GerritPluginException {
        try {
            String rawJsonString = getGerritConnector().listFiles();
            String jsonString = trimResponse(rawJsonString);
            JsonElement rootJsonElement = new JsonParser().parse(jsonString);
            for (Entry<String, JsonElement> fileList : rootJsonElement.getAsJsonObject().entrySet()) {
                JsonObject jsonObject = fileList.getValue().getAsJsonObject();
                if (jsonObject.has("status") && isMarkAsDeleted(jsonObject)) {
                    LOG.debug("[GERRIT PLUGIN] File is marked as deleted, won't comment.");
                    continue;
                }

                addFile(fileList.getKey());
            }
        } catch (IOException e) {
            throw new GerritPluginException(ERROR_LISTING, e);
        }
    }

    @NotNull
    private String trimResponse(@NotNull String response) {
        return StringUtils.replaceOnce(response, JSON_RESPONSE_PREFIX, "");
    }

    private boolean isMarkAsDeleted(JsonObject jsonObject) {
        return jsonObject.get("status").getAsCharacter() == 'D';
    }
}
