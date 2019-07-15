package fr.techad.sonar;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.fest.assertions.Assertions.assertThat;

public class GerritConfigurationTest {
    private final String SCHEME = "http";
    private final String HOST = "localhost";
    private final Integer PORT = 8080;
    private final String USERNAME = "username";
    private final String PASSWORD = "sonar";
    private final String LABEL = "Code-Review";
    private final String PROJECT = "example";
    private final String BRANCH = "example";
    private final String CHANGE_NUMBER = "changeid";
    private final String REVISION_ID = "674ac754f91e64a0efb8087e59a176484bd534d1";
    private GerritConfiguration gerritConfiguration;

    @BeforeEach
    public void setUp() {
        MapSettings settings = new MapSettings();
        settings.setProperty(PropertyKey.GERRIT_SCHEME, SCHEME).setProperty(PropertyKey.GERRIT_HOST, HOST)
            .setProperty(PropertyKey.GERRIT_PORT, PORT.toString()).setProperty(PropertyKey.GERRIT_USERNAME, USERNAME)
            .setProperty(PropertyKey.GERRIT_PASSWORD, PASSWORD).setProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_PROJECT, PROJECT).setProperty(PropertyKey.GERRIT_BRANCH, BRANCH)
            .setProperty(PropertyKey.GERRIT_CHANGE_NUMBER, CHANGE_NUMBER)
            .setProperty(PropertyKey.GERRIT_REVISION_ID, REVISION_ID)
            .setProperty(PropertyKey.GERRIT_LABEL, LABEL);
        gerritConfiguration = new GerritConfiguration(settings);
    }

    @Test
    public void shouldValidateWithDefaults() throws GerritPluginException {
        // given
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.isValid()).isTrue();
    }

    @Test
    public void shouldNotValidateIfHostIsBlank() throws GerritPluginException {
        // given
        gerritConfiguration.setHost("");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfPortIsBlank() throws GerritPluginException {
        // given
        gerritConfiguration.setPort(null);
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfLabelIsBlank() throws GerritPluginException {
        // given
        gerritConfiguration.setLabel("");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfProjectNameIsBlank() throws GerritPluginException {
        // given
        gerritConfiguration.setProjectName("");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfBranchNameIsBlank() throws GerritPluginException {
        // given
        gerritConfiguration.setProjectName("");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfChangeNumberIsBlank() throws GerritPluginException {
        // given
        gerritConfiguration.setChangeNumber("");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfRevisionIdIsBlank() throws GerritPluginException {
        // given
        gerritConfiguration.setRevisionId("");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldHandleNullBasePath() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath(null);
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/");
    }

    @Test
    public void shouldHandleEmptyBasePath() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath("");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/");
    }

    @Test
    public void shouldFixBasePathWithoutSlash() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath("gerrit");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldNotFixBasePathWithSlash() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath("/gerrit");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithSingleTrailingSlash() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath("/gerrit/");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithMultiTrailingSlashs() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath("/gerrit///");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithMultiHeadingSlashs() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath("///gerrit");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithMulitHeadingAndTrailingSlashs() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath("///gerrit///");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithMultiSlashsOnly() throws GerritPluginException {
        // given
        gerritConfiguration.setBasePath("////");
        // when
        gerritConfiguration.assertGerritConfiguration();
        // then
        assertThat(gerritConfiguration.getBasePath()).isEqualTo("/");
    }

    @Test
    public void shouldBeAnonymous() {
        gerritConfiguration.setUsername("");
        Assertions.assertTrue(gerritConfiguration.isAnonymous());
    }

    @Test
    public void shouldNotBeAnonymous() {
        gerritConfiguration.setUsername(USERNAME);
        Assertions.assertFalse(gerritConfiguration.isAnonymous());
    }

    @Test
    public void shouldBeAnonymousWithBlankUserName() {
        gerritConfiguration.setUsername("");
        Assertions.assertTrue(gerritConfiguration.isAnonymous());
        gerritConfiguration.setUsername(null);
        Assertions.assertTrue(gerritConfiguration.isAnonymous());
    }

    @Test
    public void shouldCommentNewIssuesOnly() {
        gerritConfiguration.commentNewIssuesOnly(true);
        Assertions.assertTrue(gerritConfiguration.shouldCommentNewIssuesOnly());
    }

    @Test
    public void shouldNotCommentNewIssuesOnly() {
        gerritConfiguration.commentNewIssuesOnly(false);
        Assertions.assertFalse(gerritConfiguration.shouldCommentNewIssuesOnly());
    }

    @Test
    public void shouldStrictlyCheckHostKey() {
        gerritConfiguration.strictlyCheckHostkey(true);
        Assertions.assertTrue(gerritConfiguration.shouldStrictlyCheckHostKey());
    }

    @Test
    public void shouldNotStrictlyCheckHostKey() {
        gerritConfiguration.strictlyCheckHostkey(false);
        Assertions.assertFalse(gerritConfiguration.shouldStrictlyCheckHostKey());
    }

    @Test
    public void shouldGetUsername() {
        Assertions.assertEquals(USERNAME, this.gerritConfiguration.getUsername());
    }

    @Test
    public void shouldGetPassword() {
        Assertions.assertEquals(PASSWORD, this.gerritConfiguration.getPassword());
    }

    @Test
    public void shouldGetHttpAuthScheme() {
        String httpAuthScheme = "DIGEST";
        this.gerritConfiguration.setHttpAuthScheme(httpAuthScheme);
        Assertions.assertEquals(httpAuthScheme, this.gerritConfiguration.getHttpAuthScheme());
    }

    @Test
    public void shouldGetScheme() {
        Assertions.assertEquals(SCHEME, this.gerritConfiguration.getScheme());
    }

    @Test
    public void shouldGetHost() {
        Assertions.assertEquals(HOST, this.gerritConfiguration.getHost());
    }

    @Test
    public void shouldGetPort() {
        Assertions.assertEquals(PORT, this.gerritConfiguration.getPort());
    }

    @Test
    public void shouldGetSshKeyPath() {
        String sshKeyPath = "/user/sonar";
        this.gerritConfiguration.setSshKeyPath(sshKeyPath);
        Assertions.assertEquals(sshKeyPath, this.gerritConfiguration.getSshKeyPath());
    }

    @Test
    public void shouldGetLabel() {
        Assertions.assertEquals(LABEL, this.gerritConfiguration.getLabel());
        String label = "Quality-Code";
        this.gerritConfiguration.setLabel(label);
        Assertions.assertEquals(label, this.gerritConfiguration.getLabel());
    }

    @Test
    public void shouldGetMessage() {
        String msg = "Message ${Replace}";
        this.gerritConfiguration.setMessage(msg);
        Assertions.assertEquals(msg, this.gerritConfiguration.getMessage());
    }

    @Test
    public void shouldGetIssueComment() {
        String msg = "Issue Comment";
        this.gerritConfiguration.setIssueComment(msg);
        Assertions.assertEquals(msg, this.gerritConfiguration.getIssueComment());
    }

    @Test
    public void shouldGetThreshold() {
        String threshold = "INFO";
        this.gerritConfiguration.setThreshold(threshold);
        Assertions.assertEquals(threshold, this.gerritConfiguration.getThreshold());
    }

    @Test
    public void shouldGetVoteNoIssue() {
        int vote = 2;
        this.gerritConfiguration.setVoteNoIssue(vote);
        Assertions.assertEquals(vote, this.gerritConfiguration.getVoteNoIssue());
    }

    @Test
    public void shouldGetVoteAboveThreshold() {
        int vote = 1;
        this.gerritConfiguration.setVoteAboveThreshold(vote);
        Assertions.assertEquals(vote, this.gerritConfiguration.getVoteAboveThreshold());
    }

    @Test
    public void shouldGetVoteBelowThreshold() {
        int vote = -11;
        this.gerritConfiguration.setVoteBelowThreshold(vote);
        Assertions.assertEquals(vote, this.gerritConfiguration.getVoteBelowThreshold());
    }

    @Test
    public void shouldGetProjectName() {
        Assertions.assertEquals(PROJECT, this.gerritConfiguration.getProjectName());
    }

    @Test
    public void shouldGetBranchName() {
        Assertions.assertEquals(BRANCH, this.gerritConfiguration.getBranchName());
    }

    @Test
    public void shouldGetChangeNumber() {
        Assertions.assertEquals(CHANGE_NUMBER, this.gerritConfiguration.getChangeNumber());
    }

    @Test
    public void shouldGetRevisionId() {
        Assertions.assertEquals(REVISION_ID, this.gerritConfiguration.getRevisionId());
    }

    @Test
    public void shouldInvalidateConfigurationNoUserNameInSsh() {
        gerritConfiguration.setScheme(GerritConstants.SCHEME_SSH);
        gerritConfiguration.setUsername("");
        gerritConfiguration.assertGerritConfiguration();
        Assertions.assertFalse(gerritConfiguration.isValid());
    }

    @Test
    public void shouldInvalidateConfigurationNoSshKeyPathInSsh() {
        gerritConfiguration.setScheme(GerritConstants.SCHEME_SSH);
        gerritConfiguration.setSshKeyPath("");
        gerritConfiguration.assertGerritConfiguration();
        Assertions.assertFalse(gerritConfiguration.isValid());
    }

    @Test
    public void shouldInvalidateConfigurationWithBlankLabel() {
        gerritConfiguration.setLabel("");
        gerritConfiguration.assertGerritConfiguration();
        Assertions.assertFalse(gerritConfiguration.isValid());
    }

    @Test
    public void shouldInvalidateConfigurationWithBlankProjectName() {
        gerritConfiguration.setProjectName("");
        gerritConfiguration.assertGerritConfiguration();
        Assertions.assertFalse(gerritConfiguration.isValid());
    }

    @Test
    public void shouldInvalidateConfigurationWithBlankBranchName() {
        gerritConfiguration.setBranchName("");
        gerritConfiguration.assertGerritConfiguration();
        Assertions.assertFalse(gerritConfiguration.isValid());
    }

    @Test
    public void shouldInvalidateConfigurationWithBlankChangeNumber() {
        gerritConfiguration.setChangeNumber("");
        gerritConfiguration.assertGerritConfiguration();
        Assertions.assertFalse(gerritConfiguration.isValid());
    }

    @Test
    public void shouldInvalidateConfigurationWithBlankRevisionId() {
        gerritConfiguration.setRevisionId("");
        gerritConfiguration.assertGerritConfiguration();
        Assertions.assertFalse(gerritConfiguration.isValid());
    }
}
