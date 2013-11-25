package pl.touk.sonar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.resources.Project;

@RunWith(MockitoJUnitRunner.class)
public class ReviewTest {
    @Mock
    private Project project;
    @InjectMocks
    private Review review;

    @Test(expected = GerritPluginException.class)
    public void shouldNotValidateIfGerritHostIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritHost("");
        //expect thrown
        review.validateGerritSettings();
    }

    @Test(expected = GerritPluginException.class)
    public void shouldNotValidateIfGerritPortIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritPort("");
        //expect thrown
        review.validateGerritSettings();
    }

    @Test(expected = GerritPluginException.class)
    public void shouldNotValidateIfGerritUsernameIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritUsername("");
        //expect thrown
        review.validateGerritSettings();
    }

    @Test(expected = GerritPluginException.class)
    public void shouldNotValidateIfGerritPasswordIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritPassword("");
        //expect thrown
        review.validateGerritSettings();
    }

    @Test(expected = GerritPluginException.class)
    public void shouldNotValidateIfGerritProjectNameIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritProjectName("");
        //expect thrown
        review.validateGerritSettings();
    }

    @Test(expected = GerritPluginException.class)
    public void shouldNotValidateIfGerritChangeIdIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritChangeId("");
        //expect thrown
        review.validateGerritSettings();
    }

    @Test(expected = GerritPluginException.class)
    public void shouldNotValidateIfGerritRevisionIdIsBlank() throws GerritPluginException {
        //given
        fillGerritConfiguration();
        review.setGerritRevisionId("");
        //expect thrown
        review.validateGerritSettings();
    }

    private void fillGerritConfiguration() {
        review.setGerritHost("localhost");
        review.setGerritPort("29418");
        review.setGerritUsername("sonar");
        review.setGerritProjectName("example");
        review.setGerritChangeId("myProject~master~I8473b95934b5732ac55d26311a706c9c2bde9940");
        review.setGerritRevisionId("674ac754f91e64a0efb8087e59a176484bd534d1");
    }
}
