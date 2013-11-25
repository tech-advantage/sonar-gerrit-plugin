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
public class ReviewTest {
    @Mock
    private Project projectMock;
    @Mock
    private DecoratorContext decoratorContextMock;
    @InjectMocks
    private Review review;

    @Test
    public void shouldNotValidateIfGerritHostIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritHost("");
        //when
        review.assertGerritConfiguration();
        //then
        assertThat(review.isGerritConfigurationValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfGerritPortIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritHttpPort(null);
        //when
        review.assertGerritConfiguration();
        //then
        assertThat(review.isGerritConfigurationValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfGerritUsernameIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritHttpUsername("");
        //when
        review.assertGerritConfiguration();
        //then
        assertThat(review.isGerritConfigurationValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfGerritPasswordIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritHttpPassword("");
        //when
        review.assertGerritConfiguration();
        //then
        assertThat(review.isGerritConfigurationValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfGerritProjectNameIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritProjectName("");
        //when
        review.assertGerritConfiguration();
        //then
        assertThat(review.isGerritConfigurationValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfGerritChangeIdIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritChangeId("");
        //when
        review.assertGerritConfiguration();
        //then
        assertThat(review.isGerritConfigurationValid()).isFalse();
    }

    @Test
    public void shouldNotValidateIfGerritRevisionIdIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritRevisionId("");
        //when
        review.assertGerritConfiguration();
        //then
        assertThat(review.isGerritConfigurationValid()).isFalse();
    }

    private void fillGerritConfiguration() {
        review.setGerritHost("localhost");
        review.setGerritHttpPort(8080);
        review.setGerritHttpUsername("sonar");
        review.setGerritProjectName("example");
        review.setGerritChangeId("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        review.setGerritRevisionId("674ac754f91e64a0efb8087e59a176484bd534d1");
    }
}
