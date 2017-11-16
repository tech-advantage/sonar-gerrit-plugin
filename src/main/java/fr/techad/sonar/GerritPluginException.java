package fr.techad.sonar;

public class GerritPluginException extends Exception {
    private static final long serialVersionUID = 3158628966283370707L;

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
