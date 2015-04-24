package fr.techad.sonar;

public final class PropertyKey {
    public static final String GERRIT_ENABLED = "GERRIT_ENABLED";
    public static final String GERRIT_SCHEME = "GERRIT_SCHEME";
    public static final String GERRIT_HOST = "GERRIT_HOST";
    public static final String GERRIT_HTTP_PORT = "GERRIT_HTTP_PORT";
    public static final String GERRIT_PROJECT = "GERRIT_PROJECT";
    public static final String GERRIT_BRANCH = "GERRIT_BRANCH";
    public static final String GERRIT_CHANGE_ID = "GERRIT_CHANGE_ID";
    public static final String GERRIT_REVISION_ID = "GERRIT_PATCHSET_REVISION";
    public static final String GERRIT_HTTP_USERNAME = "GERRIT_HTTP_USERNAME";
    public static final String GERRIT_HTTP_PASSWORD = "GERRIT_HTTP_PASSWORD"; //NOSONAR
    public static final String GERRIT_HTTP_AUTH_SCHEME = "GERRIT_HTTP_AUTH_SCHEME";
    public static final String GERRIT_LABEL = "GERRIT_LABEL";
    public static final String GERRIT_MESSAGE = "GERRIT_MESSAGE";
    public static final String GERRIT_BASE_PATH = "GERRIT_BASE_PATH";
    public static final String GERRIT_THRESHOLD = "GERRIT_THRESHOLD";
    public static final String GERRIT_FORCE_BRANCH = "GERRIT_FORCE_BRANCH";
    public static final String GERRIT_COMMENT_NEW_ISSUES_ONLY = "GERRIT_COMMENT_NEW_ISSUES_ONLY";

    private PropertyKey() {
    }
}
