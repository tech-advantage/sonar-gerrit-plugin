package fr.techad.sonar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.config.Settings;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritPluginException;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GerritConfigurationTest {
    @Mock
    private Settings settings;
    @Mock
    private GerritConfiguration gerritConfiguration;

    @Before
    public void setUp() {
        settings = new Settings().appendProperty(PropertyKey.GERRIT_SCHEME, "http")
                .appendProperty(PropertyKey.GERRIT_HOST, "localhost")
                .appendProperty(PropertyKey.GERRIT_HTTP_PORT, "8080")
                .appendProperty(PropertyKey.GERRIT_HTTP_USERNAME, "sonar")
                .appendProperty(PropertyKey.GERRIT_HTTP_PASSWORD, "sonar")
                .appendProperty(PropertyKey.GERRIT_BASE_PATH, "").appendProperty(PropertyKey.GERRIT_PROJECT, "example")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "example")
                .appendProperty(PropertyKey.GERRIT_CHANGE_ID, "I8473b95934b5732ac55d26311a706c9c2bde9940")
                .appendProperty(PropertyKey.GERRIT_REVISION_ID, "674ac754f91e64a0efb8087e59a176484bd534d1")
                .appendProperty(PropertyKey.GERRIT_LABEL, "Code-Review");

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
        gerritConfiguration.setHttpPort(null);
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
