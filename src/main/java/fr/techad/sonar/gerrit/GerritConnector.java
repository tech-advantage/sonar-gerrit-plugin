package fr.techad.sonar.gerrit;

import java.io.IOException;

public abstract class GerritConnector {
	public abstract String listFiles() throws IOException;

	public abstract String setReview(String reviewInputAsJson) throws IOException;
}
