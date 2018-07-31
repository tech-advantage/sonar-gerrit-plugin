package fr.techad.sonar;

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.factory.GerritFacadeFactory;
import fr.techad.sonar.mockito.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TECH ADVANTAGE
 * All right reserved
 * Created by cochon on 22/07/2018.
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class GerritInitializerTest {

    @Mock
    private GerritConfiguration gerritConfiguration;

    @Mock
    private GerritFacadeFactory gerritFacadeFactory;

    @Mock
    private GerritFacade gerritFacade;

    @Test
    public void shouldListFiles() throws GerritPluginException {
        when(gerritConfiguration.isEnabled()).thenReturn(true);
        when(gerritConfiguration.isValid()).thenReturn(true);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);

        GerritInitializer gerritInitializer = new GerritInitializer(gerritConfiguration, gerritFacadeFactory);
        gerritInitializer.execute();
        verify(gerritFacade, times(1)).listFiles();
    }

    @Test
    public void shouldNotListFilesWhenDisables() throws GerritPluginException {
        when(gerritConfiguration.isEnabled()).thenReturn(false);
        when(gerritConfiguration.isValid()).thenReturn(true);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);

        GerritInitializer gerritInitializer = new GerritInitializer(gerritConfiguration, gerritFacadeFactory);
        gerritInitializer.execute();
        verify(gerritFacade, never()).listFiles();
    }

    @Test
    public void shouldNotListFilesWhenInvalid() throws GerritPluginException {
        when(gerritConfiguration.isEnabled()).thenReturn(true);
        when(gerritConfiguration.isValid()).thenReturn(false);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);

        GerritInitializer gerritInitializer = new GerritInitializer(gerritConfiguration, gerritFacadeFactory);
        gerritInitializer.execute();
        verify(gerritFacade, never()).listFiles();
    }

    @Test
    public void shouldCatchExceptionIfListFilesThrowsIt() throws GerritPluginException {
        when(gerritConfiguration.isEnabled()).thenReturn(true);
        when(gerritConfiguration.isValid()).thenReturn(true);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        when(gerritFacade.listFiles()).thenThrow(new GerritPluginException("Mock Exception During Test"));

        GerritInitializer gerritInitializer = new GerritInitializer(gerritConfiguration, gerritFacadeFactory);
        gerritInitializer.execute();
        verify(gerritFacade, times(1)).listFiles();
    }

}
