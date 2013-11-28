package pl.touk.sonar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.resources.Project;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GerritConfigurationTest {
    @Mock
    private Project projectMock;
    @Mock
    private DecoratorContext decoratorContextMock;
    @InjectMocks
    private GerritConfiguration gerritConfiguration;

    @Test
    public void shouldNotValidateIfHostIsBlank() throws GerritPluginException {
        //given
        fillConfiguration();
        gerritConfiguration.setHost("");
        //when
        gerritConfiguration.assertGerritConfiguration();
        //then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfPortIsBlank() throws GerritPluginException {
        //given
        fillConfiguration();
        gerritConfiguration.setHttpPort(null);
        //when
        gerritConfiguration.assertGerritConfiguration();
        //then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfUsernameIsBlank() throws GerritPluginException {
        //given
        fillConfiguration();
        gerritConfiguration.setHttpUsername("");
        //when
        gerritConfiguration.assertGerritConfiguration();
        //then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfPasswordIsBlank() throws GerritPluginException {
        //given
        fillConfiguration();
        gerritConfiguration.setHttpPassword("");
        //when
        gerritConfiguration.assertGerritConfiguration();
        //then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfProjectNameIsBlank() throws GerritPluginException {
        //given
        fillConfiguration();
        gerritConfiguration.setProjectName("");
        //when
        gerritConfiguration.assertGerritConfiguration();
        //then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfChangeIdIsBlank() throws GerritPluginException {
        //given
        fillConfiguration();
        gerritConfiguration.setChangeId("");
        //when
        gerritConfiguration.assertGerritConfiguration();
        //then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfRevisionIdIsBlank() throws GerritPluginException {
        //given
        fillConfiguration();
        gerritConfiguration.setRevisionId("");
        //when
        gerritConfiguration.assertGerritConfiguration();
        //then
        assertThat(gerritConfiguration.isValid()).isFalse();
    }

    private void fillConfiguration() {
        gerritConfiguration.setHost("localhost");
        gerritConfiguration.setHttpPort(8080);
        gerritConfiguration.setHttpUsername("sonar");
        gerritConfiguration.setProjectName("example");
        gerritConfiguration.setChangeId("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        gerritConfiguration.setRevisionId("674ac754f91e64a0efb8087e59a176484bd534d1");
    }
}
