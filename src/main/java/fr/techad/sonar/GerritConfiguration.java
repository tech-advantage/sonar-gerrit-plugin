package fr.techad.sonar;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.batch.InstantiationStrategy;
import org.sonar.api.batch.ScannerSide;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
public class GerritConfiguration {
    private static final Logger LOG = Loggers.get(GerritConfiguration.class);

    private boolean enabled;
    private boolean valid;
    private boolean anonymous;
    private boolean commentNewIssuesOnly;
    private boolean strictHostkey;

    private String host;

    private String scheme;
    private Integer port;
    private String username;
    private String password;
    private String authScheme;
    private String basePath;

    private String sshKeyPath;

    private String label;
    private String message;
    private String issueComment;
    private String threshold;
    private int voteNoIssue;
    private int voteBelowThreshold;
    private int voteAboveThreshold;

    private String projectName;
    private String branchName;
    private String changeId;
    private String revisionId;

    public GerritConfiguration(Settings settings) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritConfiguration");

        this.enable(settings.getBoolean(PropertyKey.GERRIT_ENABLED));
        this.commentNewIssuesOnly(settings.getBoolean(PropertyKey.GERRIT_COMMENT_NEW_ISSUES_ONLY));
        this.strictlyCheckHostkey(settings.getBoolean(PropertyKey.GERRIT_STRICT_HOSTKEY));

        this.setScheme(settings.getString(PropertyKey.GERRIT_SCHEME));
        this.setHost(settings.getString(PropertyKey.GERRIT_HOST));
        this.setPort(settings.getInt(PropertyKey.GERRIT_PORT));

        this.setUsername(settings.getString(PropertyKey.GERRIT_USERNAME));
        this.setPassword(settings.getString(PropertyKey.GERRIT_PASSWORD));
        this.setHttpAuthScheme(settings.getString(PropertyKey.GERRIT_HTTP_AUTH_SCHEME));
        this.setBasePath(settings.getString(PropertyKey.GERRIT_BASE_PATH));

        this.setSshKeyPath(settings.getString(PropertyKey.GERRIT_SSH_KEY_PATH));

        this.setLabel(settings.getString(PropertyKey.GERRIT_LABEL));
        this.setMessage(settings.getString(PropertyKey.GERRIT_MESSAGE));
        this.setIssueComment(settings.getString(PropertyKey.GERRIT_ISSUE_COMMENT));
        this.setThreshold(settings.getString(PropertyKey.GERRIT_THRESHOLD));
        this.setVoteNoIssue(settings.getInt(PropertyKey.GERRIT_VOTE_NO_ISSUE));
        this.setVoteBelowThreshold(settings.getInt(PropertyKey.GERRIT_VOTE_ISSUE_BELOW_THRESHOLD));
        this.setVoteAboveThreshold(settings.getInt(PropertyKey.GERRIT_VOTE_ISSUE_ABOVE_THRESHOLD));

        this.setProjectName(settings.getString(PropertyKey.GERRIT_PROJECT));
        this.setBranchName(settings.getString(PropertyKey.GERRIT_BRANCH));
        this.setChangeId(settings.getString(PropertyKey.GERRIT_CHANGE_ID));
        this.setRevisionId(settings.getString(PropertyKey.GERRIT_REVISION_ID));

        this.assertGerritConfiguration();
    }

    public boolean isValid() {
        assertGerritConfiguration();
        return valid;
    }

    public GerritConfiguration enable(boolean serverEnabled) {
        enabled = serverEnabled;
        return this;
    }

    public boolean isEnabled() {
        boolean ret = enabled;
        if (StringUtils.isEmpty(changeId) || StringUtils.isEmpty(revisionId)) {
            LOG.info(
                    "[GERRIT PLUGIN] changeId or revisionId is empty. Not called from Gerrit ? Soft-disabling myself.");
            ret = false;
        }
        return ret;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public GerritConfiguration commentNewIssuesOnly(boolean newIssuesOnly) {
        commentNewIssuesOnly = newIssuesOnly;
        return this;
    }

    public boolean shouldCommentNewIssuesOnly() {
        return commentNewIssuesOnly;
    }

    public GerritConfiguration strictlyCheckHostkey(boolean strictHostkey) {
        this.strictHostkey = strictHostkey;
        return this;
    }

    public boolean shouldStrictlyCheckHostKey() {
        return strictHostkey;
    }

    public String getScheme() {
        return scheme;
    }

    public GerritConfiguration setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    public String getHost() {
        return host;
    }

    public GerritConfiguration setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public GerritConfiguration setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public GerritConfiguration setUsername(String username) {
        this.username = username;
        if (StringUtils.isBlank(username)) {
            anonymous = true;
        }
        return this;
    }

    public String getPassword() {
        return password;
    }

    public GerritConfiguration setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getHttpAuthScheme() {
        return authScheme;
    }

    public GerritConfiguration setHttpAuthScheme(String authScheme) {
        this.authScheme = authScheme;
        return this;
    }

    public String getBasePath() {
        return basePath;
    }

    public GerritConfiguration setBasePath(String basePath) {
        String newBasePath = basePath;

        if (StringUtils.isBlank(newBasePath)) {
            newBasePath = "/";
        }

        if (newBasePath.charAt(0) != '/') {
            newBasePath = "/" + newBasePath;
        }

        while (newBasePath.startsWith("/", 1) && !newBasePath.isEmpty()) {
            newBasePath = newBasePath.substring(1, newBasePath.length());
        }

        while (newBasePath.endsWith("/") && 1 < newBasePath.length()) {
            newBasePath = newBasePath.substring(0, newBasePath.length() - 1);
        }

        this.basePath = newBasePath;

        return this;
    }

    public String getSshKeyPath() {
        return sshKeyPath;
    }

    public GerritConfiguration setSshKeyPath(String sshKey) {
        this.sshKeyPath = sshKey;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public GerritConfiguration setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public GerritConfiguration setMessage(String message) {
        this.message = message;
        return this;
    }

    public void setIssueComment(String issueComment) {
        this.issueComment = issueComment;
    }

    public String getIssueComment() {
        return issueComment;
    }

    public String getThreshold() {
        return threshold;
    }

    public GerritConfiguration setThreshold(String threshold) {
        this.threshold = threshold;
        return this;
    }

    public int getVoteNoIssue() {
        return voteNoIssue;
    }

    public GerritConfiguration setVoteNoIssue(int voteNoIssue) {
        this.voteNoIssue = voteNoIssue;
        return this;
    }

    public int getVoteBelowThreshold() {
        return voteBelowThreshold;
    }

    public GerritConfiguration setVoteBelowThreshold(int voteBelowThreshold) {
        this.voteBelowThreshold = voteBelowThreshold;
        return this;
    }

    public int getVoteAboveThreshold() {
        return voteAboveThreshold;
    }

    public GerritConfiguration setVoteAboveThreshold(int voteAboveThreshold) {
        this.voteAboveThreshold = voteAboveThreshold;
        return this;
    }

    public String getProjectName() {
        return projectName;
    }

    public GerritConfiguration setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public String getBranchName() {
        return branchName;
    }

    public GerritConfiguration setBranchName(String branchName) {
        this.branchName = branchName;
        return this;
    }

    public String getChangeId() {
        return changeId;
    }

    public GerritConfiguration setChangeId(String changeId) {
        this.changeId = changeId;
        return this;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public GerritConfiguration setRevisionId(String revisionId) {
        this.revisionId = revisionId;
        return this;
    }

    void assertGerritConfiguration() {
        LOG.debug("[GERRIT PLUGIN] Verifying configuration settings …\n{}", this.toString());

        if (StringUtils.isBlank(host) || null == port) {
            valid = false;
            if (isEnabled() || LOG.isDebugEnabled()) {
                LOG.error("[GERRIT PLUGIN] ServerConfiguration is not valid : {}", this.toString());
            }
        } else {
            valid = true;
        }

        if (GerritConstants.SCHEME_SSH.equals(scheme) && StringUtils.isBlank(username)) {
            valid = false;
            if (isEnabled() || LOG.isDebugEnabled()) {
                LOG.error("[GERRIT PLUGIN] Scheme is ssh but username is blank : {}", this.toString());
            }
        }

        if (GerritConstants.SCHEME_SSH.equals(scheme) && StringUtils.isBlank(sshKeyPath)) {
            valid = false;
            if (isEnabled() || LOG.isDebugEnabled()) {
                LOG.error("[GERRIT PLUGIN] Scheme is ssh but keypath is blank : {}", this.toString());
            }
        }

        if (StringUtils.isBlank(label) || StringUtils.isBlank(projectName) || StringUtils.isBlank(branchName)
                || StringUtils.isBlank(changeId) || StringUtils.isBlank(revisionId)) {
            valid = false;
            if (isEnabled() || LOG.isDebugEnabled()) {
                LOG.error("[GERRIT PLUGIN] ReviewConfiguration is not valid : {}", this.toString());
            }
        } else {
            valid &= true;
        }
    }

    @Override
    public String toString() {
        return "GerritConfiguration [valid=" + valid + ", enabled=" + enabled + ", scheme=" + scheme + ", host=" + host
                + ", port=" + port + ", anonymous=" + anonymous + ", username=" + username + ", password="
                + (StringUtils.isBlank(password) ? "blank" : "*obfuscated*") + ", authScheme=" + authScheme
                + ", basePath=" + basePath + ", sshKeyPath=" + sshKeyPath + ", label=" + label + ", message=" + message
                + ", issueComment=" + issueComment + ", threshold=" + threshold + ", voteNoIssue=" + voteNoIssue
                + ",voteBelowThreshold=" + voteBelowThreshold + ",voteAboveThreshold=" + voteAboveThreshold
                + ",commentNewIssuesOnly=" + commentNewIssuesOnly + ", projectName=" + projectName + ", branchName="
                + branchName + ", changeId=" + changeId + ", revisionId=" + revisionId + "]";
    }
}
