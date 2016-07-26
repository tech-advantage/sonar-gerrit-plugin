package fr.techad.sonar.gerrit;

import java.io.IOException;

public interface GerritConnector {
    public String listFiles() throws IOException;

    public String setReview(String reviewInputAsJson) throws IOException;
}
