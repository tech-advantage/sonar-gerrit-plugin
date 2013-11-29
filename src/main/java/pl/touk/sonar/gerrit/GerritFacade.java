package pl.touk.sonar.gerrit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import pl.touk.sonar.GerritPluginException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GerritFacade {
    private static final String RESPONSE_PREFIX = ")]}'";
    private static final String COMMIT_MSG = "/COMMIT_MSG";
    private static final String MAVEN_ENTRY_REGEX = ".*src/(main|test)/java/";
    private static final String DOT = ".";
    private GerritConnector gerritConnector;
    private ObjectMapper objectMapper = new ObjectMapper();

    public GerritFacade(@NotNull String host, int port, @NotNull String username, @NotNull String password) {
        gerritConnector = new GerritConnector(host, port, username, password);
    }

    /**
     * @return sonarLongName to gerritFileName map
     */
    @NotNull
    public Map<String, String> listFiles(@NotNull String changeId, @NotNull String revisionId) throws GerritPluginException {
        try {
            String response = gerritConnector.listFiles(changeId, revisionId);
            String jsonString = trimResponse(response);
            ListFilesResponse listFilesResponse = objectMapper.readValue(jsonString, ListFilesResponse.class);
            Map<String, String> files = new HashMap<String, String>();
            Set<String> keys = listFilesResponse.keySet();
            keys.remove(COMMIT_MSG);
            for (String key : keys) {
                files.put(parseFileName(key), key);
            }
            return files;
        } catch (IOException e) {
            throw new GerritPluginException("Error listing files", e);
        } catch (URISyntaxException e) {
            throw new GerritPluginException("Error listing files", e);
        }
    }

    public void setReview(@NotNull String changeId, @NotNull String revisionId, @NotNull ReviewInput reviewInput) throws GerritPluginException {
        try {
            String json = objectMapper.writeValueAsString(reviewInput);
            gerritConnector.setReview(changeId, revisionId, json);
        } catch (JsonProcessingException e) {
            throw new GerritPluginException("Error setting review", e);
        } catch (IOException e) {
            throw new GerritPluginException("Error setting review", e);
        } catch (URISyntaxException e) {
            throw new GerritPluginException("Error setting review", e);
        }
    }

    @NotNull
    protected String trimResponse(@NotNull String response) {
        return StringUtils.replaceOnce(response, RESPONSE_PREFIX, "");
    }

    protected String parseFileName(@NotNull String fileName) {
        return StringUtils.substringBeforeLast(fileName.replaceFirst(MAVEN_ENTRY_REGEX, ""), DOT).replace('/', '.');
    }

    void setGerritConnector(@NotNull GerritConnector gerritConnector) {
        this.gerritConnector = gerritConnector;
    }
}
