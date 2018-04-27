package fr.techad.sonar.gerrit;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.PropertyKey;
import fr.techad.sonar.gerrit.factory.GerritConnectorFactory;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class GerritFacadeTest {

    private GerritFacade facade = null;

    @Before
    public void setUp() {
        MapSettings settings = new MapSettings();
        settings.setProperty(PropertyKey.GERRIT_SCHEME, "http").setProperty(PropertyKey.GERRIT_HOST, "localhost")
            .setProperty(PropertyKey.GERRIT_PORT, "8080").setProperty(PropertyKey.GERRIT_USERNAME, "sonar")
            .setProperty(PropertyKey.GERRIT_PASSWORD, "sonar").setProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_PROJECT, "project")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch")
            .setProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
            .setProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
            .setProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        // when
        GerritConnectorFactory connectorFactory = new GerritConnectorFactory(gerritConfiguration);
        facade = new GerritFacade(connectorFactory.getConnector()) {

            @Override
            protected void fillListFilesFomGerrit() throws GerritPluginException {
            }
        };
    }

    @Test
    public void testParseFileName() {
        assertThat(facade.parseFileName("subdirectory/src/fr/techad/sonar/gerrit/GerritFacadeTest.java"),
            is("src/fr/techad/sonar/gerrit/GerritFacadeTest.java"));
        assertThat(facade.parseFileName("fr/techad/sonar/gerrit/GerritFacadeTest.java"),
            is("fr/techad/sonar/gerrit/GerritFacadeTest.java"));
        assertThat(facade.parseFileName("sub1/sub2/sub3/sub4/src/fr/techad/sonar/gerrit/GerritFacadeTest.java"),
            is("src/fr/techad/sonar/gerrit/GerritFacadeTest.java"));
        assertThat(facade.parseFileName("subdirectory/src/main/java/src/fr/techad/sonar/gerrit/GerritFacadeTest.java"),
            is("src/main/java/src/fr/techad/sonar/gerrit/GerritFacadeTest.java"));
        assertThat(facade.parseFileName("src/main/java/src/fr/techad/sonar/gerrit/GerritFacadeTest.java"),
            is("src/main/java/src/fr/techad/sonar/gerrit/GerritFacadeTest.java"));
        assertThat(facade.parseFileName("/src/main/java/src/fr/techad/sonar/gerrit/GerritFacadeTest.java"),
            is("src/main/java/src/fr/techad/sonar/gerrit/GerritFacadeTest.java"));
    }

}
