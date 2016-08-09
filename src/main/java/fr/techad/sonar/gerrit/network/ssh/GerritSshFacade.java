package fr.techad.sonar.gerrit.network.ssh;

import java.io.IOException;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.factory.GerritConnectorFactory;

public class GerritSshFacade extends GerritFacade {
    private static final Logger LOG = Loggers.get(GerritSshFacade.class);
    private static final String COMMIT_MSG = "/COMMIT_MSG";
    private static final String ERROR_LISTING = "Error listing files";

    public GerritSshFacade(GerritConnectorFactory gerritConnectorFactory) {
        super(gerritConnectorFactory);
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritSshFacade");
    }

    @Override
    protected void fillListFilesFomGerrit() throws GerritPluginException {
        try {
            String rawJsonString = getGerritConnector().listFiles();
            JsonArray files = new JsonParser().parse(rawJsonString.split("\r?\n")[0]).getAsJsonObject()
                    .getAsJsonObject("currentPatchSet").getAsJsonArray("files");

            for (JsonElement jsonElement : files) {
                String fileName = jsonElement.getAsJsonObject().get("file").getAsString();
                if (COMMIT_MSG.equals(fileName)) {
                    continue;
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("type") && isMarkAsDeleted(jsonObject)) {
                    LOG.debug("[GERRIT PLUGIN] File is marked as deleted, won't comment.");
                    continue;
                }

                addFile(fileName);
            }
        } catch (IOException e) {
            throw new GerritPluginException(ERROR_LISTING, e);
        }
    }

    private boolean isMarkAsDeleted(JsonObject jsonObject) {
        return "DELETED".equals(jsonObject.get("type").getAsString());
    }

}
