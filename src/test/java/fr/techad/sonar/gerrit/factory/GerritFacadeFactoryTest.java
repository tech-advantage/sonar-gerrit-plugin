package fr.techad.sonar.gerrit.factory;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritConstants;
import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.network.rest.GerritRestFacade;
import fr.techad.sonar.gerrit.network.ssh.GerritSshFacade;
import fr.techad.sonar.mockito.MockitoExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class GerritFacadeFactoryTest {
    @Mock
    private GerritConfiguration gerritConfiguration;

    @Test
    public void shouldGetFacadeAsGerritRestFaceWhenConnectorIsGerritRestConnector() throws Exception {
        Mockito.when(gerritConfiguration.getScheme()).thenReturn(GerritConstants.SCHEME_HTTP);
        GerritConnectorFactory gerritConnectorFactory = new GerritConnectorFactory(gerritConfiguration);
        GerritFacadeFactory gerritFacadeFactory = new GerritFacadeFactory(gerritConnectorFactory);
        GerritFacade gerritFacade = gerritFacadeFactory.getFacade();
        assertEquals(true, gerritFacade instanceof GerritRestFacade);
    }

    @Test
    public void shouldGetFacadeAsGerritSshFacadeWhenConnectorIsGerritSshConnector() throws Exception {
        Mockito.when(gerritConfiguration.getScheme()).thenReturn(GerritConstants.SCHEME_SSH);
        Mockito.when(gerritConfiguration.getUsername()).thenReturn("user");
        Mockito.when(gerritConfiguration.getHost()).thenReturn("localhost");
        Mockito.when(gerritConfiguration.getPort()).thenReturn(22);
        GerritConnectorFactory gerritConnectorFactory = new GerritConnectorFactory(gerritConfiguration);
        GerritFacadeFactory gerritFacadeFactory = new GerritFacadeFactory(gerritConnectorFactory);
        GerritFacade gerritFacade = gerritFacadeFactory.getFacade();
        assertEquals(true, gerritFacade instanceof GerritSshFacade);
    }

    @Test
    public void shouldGetFacadeAsNullWhenConnectorIsUnknown() throws Exception {
        Mockito.when(gerritConfiguration.getScheme()).thenReturn("UnknownProtocol");
        GerritConnectorFactory gerritConnectorFactory = new GerritConnectorFactory(gerritConfiguration);
        GerritFacadeFactory gerritFacadeFactory = new GerritFacadeFactory(gerritConnectorFactory);
        GerritFacade gerritFacade = gerritFacadeFactory.getFacade();
        assertEquals(null, gerritFacade);
    }

}
