package pl.touk.sonar.gerrit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gerrit request for review input.
 * Used with JSON marshaller only.
 *
 * Example JSON:
 *
 * {
 * "message": "Some nits need to be fixed.",
 * "labels": {
 * "Code-Review": -1
 * },
 * "comments": {
 * "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java": [
 * {
 * "line": 23,
 * "message": "[nit] trailing whitespace"
 * },
 * {
 * "line": 49,
 * "message": "[nit] s/conrtol/control"
 * }
 * ]
 * }
 * }
 */
public class ReviewInput {
    public String message = "Looks good to me.";
    public Map<String, Integer> labels = new HashMap<String, Integer>();
    public Map<String, List<ReviewFileComment>> comments = new HashMap<String, List<ReviewFileComment>>();

    public void setLabelToPlusOne(String label) {
        labels.put(label, 1);
    }

    public void setLabelToMinusOne(String label) {
        labels.put(label, -1);
    }
}
