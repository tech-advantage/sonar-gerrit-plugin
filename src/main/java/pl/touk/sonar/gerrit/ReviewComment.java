package pl.touk.sonar.gerrit;

/**
 * Gerrit comment used with request for review input.
 * Used with JSON marshaller only.
 */
public class ReviewComment {
    public Integer line;
    public String message;
}
