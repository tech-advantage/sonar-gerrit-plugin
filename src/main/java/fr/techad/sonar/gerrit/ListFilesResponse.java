package fr.techad.sonar.gerrit;

import java.util.HashMap;
import java.util.Map;

/**
 * Gerrit response to ListFiles model. Used with JSON unmarshaller only.
 */
public class ListFilesResponse extends HashMap<String, Map<String, String>> {

    private static final long serialVersionUID = -8867590486493558117L;
}
