package fr.techad.sonar.gerrit;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.google.gson.stream.JsonWriter;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.review.ReviewFileComment;
import fr.techad.sonar.gerrit.review.ReviewInput;
import fr.techad.sonar.gerrit.review.ReviewLineComment;

public abstract class GerritFacade {
    private static final Logger LOG = Loggers.get(GerritFacade.class);
    private static final String MAVEN_ENTRY_REGEX = ".*?/?src/";
    private static final String ERROR_FORMAT = "Error formatting review";
    private static final String ERROR_SETTING = "Error setting review";
    private static final String COMMIT_MSG = "/COMMIT_MSG";

    private final GerritConnector gerritConnector;
    private List<String> gerritFileList = new ArrayList<>();

    public GerritFacade(GerritConnector gerritConnector) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritFacade");
        this.gerritConnector = gerritConnector;
    }

    @NotNull
    public List<String> listFiles() throws GerritPluginException {
        if (!gerritFileList.isEmpty()) {
            LOG.debug("[GERRIT PLUGIN] File list already filled. Not calling Gerrit.");
        } else {
            fillListFilesFomGerrit();
        }
        return Collections.unmodifiableList(gerritFileList);
    }

    public void setReview(@NotNull ReviewInput reviewInput) throws GerritPluginException {
        try {
            gerritConnector.setReview(formatReview(reviewInput));
        } catch (IOException e) {
            throw new GerritPluginException(ERROR_SETTING, e);
        }
    }

    public String parseFileName(@NotNull String fileName) {
        LOG.debug("[GERRIT PLUGIN] parse filename: {}.", fileName);
        return fileName.replaceFirst(MAVEN_ENTRY_REGEX, "src/");
    }

    protected GerritConnector getGerritConnector() {
        return this.gerritConnector;
    }

    protected void addFile(String fileName) {
        if (COMMIT_MSG.equals(fileName)) {
            LOG.debug("[GERRIT PLUGIN] File is commit message, not adding");
        } else {
            gerritFileList.add(fileName);
        }
    }

    protected abstract void fillListFilesFomGerrit() throws GerritPluginException;

    @NotNull
    private String formatReview(ReviewInput reviewInput) throws GerritPluginException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(stringWriter);

        try {
            jsonWriter.beginObject();
            jsonWriter.name("message").value(reviewInput.getMessage());

            if (!reviewInput.getLabels().isEmpty()) {
                jsonWriter.name("labels").beginObject();
                for (String label : reviewInput.getLabels().keySet()) {
                    jsonWriter.name(label).value(reviewInput.getLabels().get(label));
                }
                jsonWriter.endObject();
            }

            if (!reviewInput.getComments().isEmpty()) {
                jsonWriter.name("comments").beginObject();
                for (String fileName : reviewInput.getComments().keySet()) {
                    if (!reviewInput.getComments().isEmpty()) {
                        jsonWriter.name(fileName).beginArray();
                        for (ReviewFileComment rfc : reviewInput.getComments().get(fileName)) {
                            jsonWriter.beginObject();
                            jsonWriter.name("line").value(((ReviewLineComment) rfc).getLine());
                            jsonWriter.name("message").value(rfc.getMessage());
                            jsonWriter.endObject();
                        }
                        jsonWriter.endArray();
                    }
                }
                jsonWriter.endObject();
            }
            jsonWriter.endObject();
            jsonWriter.close();
        } catch (IOException e) {
            throw new GerritPluginException(ERROR_FORMAT, e);
        }

        return stringWriter.toString();
    }
}
