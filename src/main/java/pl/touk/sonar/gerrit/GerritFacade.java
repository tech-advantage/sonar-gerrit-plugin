package pl.touk.sonar.gerrit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.codehaus.plexus.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import pl.touk.sonar.GerritPluginException;

public class GerritFacade {
    private static final String RESPONSE_PREFIX = ")]}'";
    private static final String COMMIT_MSG = "/COMMIT_MSG";
    private GerritConnector gerritConnector;
    private ObjectMapper objectMapper = new ObjectMapper();

    public GerritFacade(String host, int port, String username, String password) {
        gerritConnector = new GerritConnector(host, port, username, password);
    }

    @NotNull
    public List<String> listFiles(String changeId, String revisionId) throws GerritPluginException {
        try {
            String response = gerritConnector.listFiles(changeId, revisionId);
            String jsonString = trimResponse(response);
            ListFilesResponse listFilesResponse = objectMapper.readValue(jsonString, ListFilesResponse.class);
            List<String> files = new ArrayList<String>();
            Set<String> keys = listFilesResponse.keySet();
            keys.remove(COMMIT_MSG);
            files.addAll(keys);
            return files;

        } catch (IOException e) {
            throw new GerritPluginException("Error listing files", e);
        } catch (URISyntaxException e) {
            throw new GerritPluginException("Error listing files", e);
        }
    }

    @NotNull
    protected String trimResponse(@NotNull String response) {
        return StringUtils.replaceOnce(response, RESPONSE_PREFIX, "");
    }

    void setGerritConnector(@NotNull GerritConnector gerritConnector) {
        this.gerritConnector = gerritConnector;
    }
}
