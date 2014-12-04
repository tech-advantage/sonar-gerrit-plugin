package fr.techad.sonar.gerrit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/**
 * Gerrit request for review input. Used with JSON marshaller only.
 *
 * Example JSON:
 *
 * { "message": "Some nits need to be fixed.", "labels": { "Code-Review": -1 },
 * "comments": {
 * "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java"
 * : [ { "line": 23, "message": "[nit] trailing whitespace" }, { "line": 49,
 * "message": "[nit] s/conrtol/control" } ] } }
 */
public class ReviewInput {
    private String message = "Looks good to me.";
    private Map<String, Integer> labels = new ConcurrentHashMap<String, Integer>();
    private Map<String, List<ReviewFileComment>> comments = new ConcurrentHashMap<String, List<ReviewFileComment>>();

    public void setLabelToPlusOne(@NotNull String label) {
        labels.put(label, 1);
    }

    public void setLabelToMinusOne(@NotNull String label) {
        labels.put(label, -1);
    }

    public void setMessage(@NotNull String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void addComments(String key, List<ReviewFileComment> reviewFileComments) {
        comments.put(key, reviewFileComments);
    }

    public int size() {
        return comments.size();
    }

    public void emptyComments() {
        comments.clear();
    }

    public Map<String, Integer> getLabels() {
        return labels;
    }

    public Map<String, List<ReviewFileComment>> getComments() {
        return comments;
    }

    @Override
    public String toString() {
        return "ReviewInput [message=" + message + ", labels=" + labels + ", comments=" + comments + "]";
    }
}
