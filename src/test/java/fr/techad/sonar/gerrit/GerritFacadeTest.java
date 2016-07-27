package fr.techad.sonar.gerrit;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Map;

import org.junit.Test;

public class GerritFacadeTest {
	
	private GerritFacade facade = new GerritFacade() {
	    public Map<String, String> listFiles() { return null;};
		public void setReview(ReviewInput reviewInput) {};
	};

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
