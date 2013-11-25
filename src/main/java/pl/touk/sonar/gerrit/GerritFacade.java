package pl.touk.sonar.gerrit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import pl.touk.sonar.GerritPluginException;
import pl.touk.sonar.Review;

public class GerritFacade {
    private static final String RESPONSE_PREFIX = ")]}'";
    private static final String COMMIT_MSG = "/COMMIT_MSG";
    private static final String MAVEN_ENTRY = "src/main/java/";
    private static final String DOT = ".";
    private GerritConnector gerritConnector;
    private ObjectMapper objectMapper = new ObjectMapper();

    public GerritFacade(@NotNull String host, int port, @NotNull String username, @NotNull String password) {
        gerritConnector = new GerritConnector(host, port, username, password);
    }

    @NotNull
    public List<String> listFiles(@NotNull String changeId, @NotNull String revisionId) throws GerritPluginException {
        try {
            String response = gerritConnector.listFiles(changeId, revisionId);
            String jsonString = trimResponse(response);
            ListFilesResponse listFilesResponse = objectMapper.readValue(jsonString, ListFilesResponse.class);
            List<String> files = new ArrayList<String>();
            Set<String> keys = listFilesResponse.keySet();
            keys.remove(COMMIT_MSG);
            for (String key : keys) {
                files.add(parseFileName(key));
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
        return StringUtils.substringBeforeLast(StringUtils.substringAfter(fileName, MAVEN_ENTRY), DOT).replace('/', '.');
    }

    void setGerritConnector(@NotNull GerritConnector gerritConnector) {
        this.gerritConnector = gerritConnector;
    }
}
