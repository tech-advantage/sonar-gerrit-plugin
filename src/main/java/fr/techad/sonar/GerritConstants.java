package fr.techad.sonar;

public final class GerritConstants {

    public static final String GERRIT_CATEGORY = "Gerrit";
    public static final String GERRIT_SUBCATEGORY_SERVER = "Server";
    public static final String GERRIT_SUBCATEGORY_REVIEW = "Review";
    public static final String GERRIT_ENABLED_DEFAULT = "true";
    public static final String GERRIT_FORCE_BRANCH_DEFAULT = "false";
    public static final String SCHEME_HTTP = "http";
    public static final String SCHEME_HTTPS = "https";
    public static final String SCHEME_SSH = "ssh";
    public static final String AUTH_BASIC = "basic";
    public static final String AUTH_DIGEST = "digest";
    public static final String GERRIT_COMMENT_NEW_ISSUES_ONLY = "false";
    public static final String GERRIT_STRICT_HOSTKEY_DEFAULT = "true";
    public static final String GERRIT_VOTE_NO_ISSUE_DEFAULT = "+1";
    public static final String GERRIT_VOTE_ISSUE_BELOW_THRESHOLD_DEFAULT = "+1";
    public static final String GERRIT_VOTE_ISSUE_ABOVE_THRESHOLD_DEFAULT = "-1";

    private GerritConstants() {
        throw new IllegalAccessError("Utility class");
    }

}
