package fr.techad.sonar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.fest.assertions.Assertions.assertThat;

public class GerritConfigurationTest {

    private GerritConfiguration gerritConfiguration;

    @BeforeEach
    public void setUp() {
        MapSettings settings = new MapSettings();
        settings.setProperty(PropertyKey.GERRIT_SCHEME, "http").setProperty(PropertyKey.GERRIT_HOST, "localhost")
            .setProperty(PropertyKey.GERRIT_PORT, "8080").setProperty(PropertyKey.GERRIT_USERNAME, "sonar")
            .setProperty(PropertyKey.GERRIT_PASSWORD, "sonar").setProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_PROJECT, "example").setProperty(PropertyKey.GERRIT_BRANCH, "example")
            .setProperty(PropertyKey.GERRIT_CHANGE_ID, "I8473b95934b5732ac55d26311a706c9c2bde9940")
            .setProperty(PropertyKey.GERRIT_REVISION_ID, "674ac754f91e64a0efb8087e59a176484bd534d1")
            .setProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
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
    public void shouldNotValidateIfChangeIdIsBlank() throws GerritPluginException {
        // given
        gerritConfiguration.setChangeId("");
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
}
