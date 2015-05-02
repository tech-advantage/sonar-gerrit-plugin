package fr.techad.sonar.gerrit;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.BatchComponent;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.techad.sonar.GerritPluginException;

@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritFacade implements BatchComponent {
    private static final Logger LOG = Loggers.get(GerritFacade.class);
    private static final String RESPONSE_PREFIX = ")]}'";
    private static final String COMMIT_MSG = "/COMMIT_MSG";
    private static final String MAVEN_ENTRY_REGEX = ".*src/";

    private static final String ERROR_LISTING = "Error listing files";
    private static final String ERROR_SETTING = "Error setting review";
    private final GerritConnector gerritConnector;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String> gerritFileList = new HashMap<String, String>();

    public GerritFacade(GerritConnector gerritConnector) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritFacade");
        this.gerritConnector = gerritConnector;
    }

    /**
     * @return sonarLongName to gerritFileName map
     */
    @NotNull
    public Map<String, String> listFiles() throws GerritPluginException {
        if (!gerritFileList.isEmpty()) {
            LOG.debug("[GERRIT PLUGIN] File list already filled. Not calling Gerrit.");
        } else {
            try {
                String response = gerritConnector.listFiles();
                String jsonString = trimResponse(response);
                ListFilesResponse listFilesResponse = objectMapper.readValue(jsonString, ListFilesResponse.class);
                Set<String> keys = listFilesResponse.keySet();
                keys.remove(COMMIT_MSG);
                for (String key : keys) {
                    gerritFileList.put(parseFileName(key), key);
                }
            } catch (IOException e) {
                throw new GerritPluginException(ERROR_LISTING, e);
            }
        }
        return Collections.unmodifiableMap(gerritFileList);
    }

    public void setReview(@NotNull ReviewInput reviewInput) throws GerritPluginException {
        try {
            String json = objectMapper.writeValueAsString(reviewInput);
            gerritConnector.setReview(json);
        } catch (JsonProcessingException e) {
            throw new GerritPluginException(ERROR_SETTING, e);
        } catch (IOException e) {
            throw new GerritPluginException(ERROR_SETTING, e);
        }
    }

    @NotNull
    protected String trimResponse(@NotNull String response) {
        return StringUtils.replaceOnce(response, RESPONSE_PREFIX, "");
    }

    protected String parseFileName(@NotNull String fileName) {
        return fileName.replaceFirst(MAVEN_ENTRY_REGEX, "src/");
    }
}
