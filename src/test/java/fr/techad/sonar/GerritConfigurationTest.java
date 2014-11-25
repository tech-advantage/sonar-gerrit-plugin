package fr.techad.sonar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.resources.Project;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.GerritConfiguration.GerritReviewConfiguration;
import fr.techad.sonar.GerritConfiguration.GerritServerConfiguration;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GerritConfigurationTest {
    @Mock
    private Project projectMock;
    @Mock
    private DecoratorContext decoratorContextMock;
    @InjectMocks
    private GerritServerConfiguration gerritServerConfiguration = GerritConfiguration.serverConfiguration();
    @InjectMocks
    private GerritReviewConfiguration gerritReviewConfiguration = GerritConfiguration.reviewConfiguration();

    @Test
    public void shouldValidateWithDefaults() throws GerritPluginException {
        // given
        fillConfiguration();
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.isValid()).isTrue();
    }

    @Test
    public void shouldNotValidateIfHostIsBlank() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setHost("");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfPortIsBlank() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setHttpPort(null);
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfLabelIsBlank() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritReviewConfiguration.setLabel("");
        // when
        gerritReviewConfiguration.assertGerritReviewConfiguration();
        // then
        assertThat(gerritReviewConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfProjectNameIsBlank() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritReviewConfiguration.setProjectName("");
        // when
        gerritReviewConfiguration.assertGerritReviewConfiguration();
        // then
        assertThat(gerritReviewConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfBranchNameIsBlank() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritReviewConfiguration.setProjectName("");
        // when
        gerritReviewConfiguration.assertGerritReviewConfiguration();
        // then
        assertThat(gerritReviewConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfChangeIdIsBlank() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritReviewConfiguration.setChangeId("");
        // when
        gerritReviewConfiguration.assertGerritReviewConfiguration();
        // then
        assertThat(gerritReviewConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfRevisionIdIsBlank() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritReviewConfiguration.setRevisionId("");
        // when
        gerritReviewConfiguration.assertGerritReviewConfiguration();
        // then
        assertThat(gerritReviewConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldHandleNullBasePath() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath(null);
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/");
    }

    @Test
    public void shouldHandleEmptyBasePath() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath("");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/");
    }

    @Test
    public void shouldFixBasePathWithoutSlash() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath("gerrit");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldNotFixBasePathWithSlash() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath("/gerrit");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithSingleTrailingSlash() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath("/gerrit/");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithMultiTrailingSlashs() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath("/gerrit///");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithMultiHeadingSlashs() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath("///gerrit");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithMulitHeadingAndTrailingSlashs() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath("///gerrit///");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/gerrit");
    }

    @Test
    public void shouldFixBasePathWithMultiSlashsOnly() throws GerritPluginException {
        // given
        fillConfiguration();
        gerritServerConfiguration.setBasePath("////");
        // when
        gerritServerConfiguration.assertGerritServerConfiguration();
        // then
        assertThat(gerritServerConfiguration.getBasePath()).isEqualTo("/");
    }

    private void fillConfiguration() {
        gerritServerConfiguration.setScheme("http");
        gerritServerConfiguration.setHost("localhost");
        gerritServerConfiguration.setHttpPort(8080);
        gerritServerConfiguration.setHttpUsername("sonar");
        gerritServerConfiguration.setHttpPassword("sonar");
        gerritServerConfiguration.setBasePath("");
        gerritReviewConfiguration.setProjectName("example");
        gerritReviewConfiguration.setBranchName("example");
        // Initial "myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940"
        // Now "I8473b95934b5732ac55d26311a706c9c2bde9940"
        gerritReviewConfiguration.setChangeId("I8473b95934b5732ac55d26311a706c9c2bde9940");
        gerritReviewConfiguration.setRevisionId("674ac754f91e64a0efb8087e59a176484bd534d1");
        gerritReviewConfiguration.setLabel("Code-Review");
    }
}
