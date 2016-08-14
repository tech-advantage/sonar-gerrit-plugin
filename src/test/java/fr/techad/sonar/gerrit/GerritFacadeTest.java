package fr.techad.sonar.gerrit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.config.Settings;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.PropertyKey;
import fr.techad.sonar.gerrit.factory.GerritConnectorFactory;

public class GerritFacadeTest {
	
	private GerritFacade facade = null;
	
	@Before
	public void setUp() {
	    Settings settings = new Settings().appendProperty(PropertyKey.GERRIT_SCHEME, "http")
                .appendProperty(PropertyKey.GERRIT_HOST, "localhost").appendProperty(PropertyKey.GERRIT_PORT, "8080")
                .appendProperty(PropertyKey.GERRIT_USERNAME, "sonar")
                .appendProperty(PropertyKey.GERRIT_PASSWORD, "sonar").appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
                .appendProperty(PropertyKey.GERRIT_PROJECT, "project")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch")
                .appendProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
                .appendProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
                .appendProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
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
