package pl.touk.sonar;

public class GerritPluginException extends Exception {
    public GerritPluginException() {
        super();
    }

    public GerritPluginException(String message) {
        super(message);
    }

    public GerritPluginException(String message, Throwable cause) {
        super(message, cause);
    }
}
