package fr.techad.sonar;

public final class PropertyKey {
    public static final String GERRIT_ENABLED = "GERRIT_ENABLED";
    public static final String GERRIT_SCHEME = "GERRIT_SCHEME";
    public static final String GERRIT_HOST = "GERRIT_HOST";
    public static final String GERRIT_PORT = "GERRIT_PORT";
    public static final String GERRIT_PROJECT = "GERRIT_PROJECT";
    public static final String GERRIT_BRANCH = "GERRIT_BRANCH";
    public static final String GERRIT_CHANGE_ID = "GERRIT_CHANGE_ID";
    public static final String GERRIT_REVISION_ID = "GERRIT_PATCHSET_REVISION";
    public static final String GERRIT_USERNAME = "GERRIT_USERNAME";
    public static final String GERRIT_PASSWORD = "GERRIT_PASSWORD"; // NOSONAR
    public static final String GERRIT_SSH_KEY_PATH = "GERRIT_SSH_KEY_PATH";
    public static final String GERRIT_HTTP_AUTH_SCHEME = "GERRIT_HTTP_AUTH_SCHEME";
    public static final String GERRIT_LABEL = "GERRIT_LABEL";
    public static final String GERRIT_MESSAGE = "GERRIT_MESSAGE";
    public static final String GERRIT_BASE_PATH = "GERRIT_BASE_PATH";
    public static final String GERRIT_THRESHOLD = "GERRIT_THRESHOLD";
    public static final String GERRIT_FORCE_BRANCH = "GERRIT_FORCE_BRANCH";
    public static final String GERRIT_COMMENT_NEW_ISSUES_ONLY = "GERRIT_COMMENT_NEW_ISSUES_ONLY";
    public static final String GERRIT_VOTE_NO_ISSUE = "GERRIT_VOTE_NO_ISSUE";
    public static final String GERRIT_VOTE_ISSUE_BELOW_THRESHOLD = "GERRIT_VOTE_ISSUE_BELOW_THRESHOLD";
    public static final String GERRIT_VOTE_ISSUE_ABOVE_THRESHOLD = "GERRIT_VOTE_ISSUE_ABOVE_THRESHOLD";
    public static final String GERRIT_ISSUE_COMMENT = "GERRIT_ISSUE_COMMENT";
    public static final String GERRIT_STRICT_HOSTKEY = "GERRIT_STRICT_HOSTKEY";

    private PropertyKey() {
    }
}
