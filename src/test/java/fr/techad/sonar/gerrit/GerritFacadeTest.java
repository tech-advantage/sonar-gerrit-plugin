package fr.techad.sonar.gerrit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.GerritConnector;
import fr.techad.sonar.gerrit.GerritFacade;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GerritFacadeTest {
    private static final String SCHEME = "https";
    private static final String HOST = "localhost";
    private static final int PORT = 443;
    private static final String USERNAME = "sonar";
    private static final String PASSWORD = "password";
    private static final String AUTH_SCHEME = "digest";
    private static final String BASE_PATH = "";
    private static final String PROJECT_NAME = "myProject";
    private static final String BRANCH_NAME = "master";
    private static final String CHANGE_ID = "I8473b95934b5732ac55d26311a706c9c2bde9940";
    private static final String REVISION_ID = "674ac754f91e64a0efb8087e59a176484bd534d1";
    private static final String LIST_FILES_RESPONSE = ")]}'\n" + "  {\n" + "    \"/COMMIT_MSG\": {\n"
            + "      \"status\": \"A\",\n" + "      \"lines_inserted\": 7\n" + "    },\n"
            + "    \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\": {\n"
            + "      \"lines_inserted\": 5,\n" + "      \"lines_deleted\": 3\n" + "    },\n"
            + "    \"gerrit-server/src/test/java/com/google/gerrit/server/project/RefControlTest.java\": {\n"
            + "      \"lines_inserted\": 5,\n" + "      \"lines_deleted\": 3\n" + "    },\n"
            + "    \"gerrit-server/src/com/google/gerrit/server/project/RefControl2.java\": {\n"
            + "      \"lines_inserted\": 5,\n" + "      \"lines_deleted\": 3\n" + "    }\n" + "  }";
    @Mock
    private GerritConnector gerritConnectorMock;
    private GerritFacade gerritFacade;

    @Before
    public void setUp() {
        gerritFacade = new GerritFacade(SCHEME, HOST, PORT, USERNAME, PASSWORD, BASE_PATH, AUTH_SCHEME);
        gerritFacade.setGerritConnector(gerritConnectorMock);
    }

    @Test
    public void trimShouldTrimResponseFromPrefix() {
        // given
        String response = ")]}'{}";
        // expect
        assertThat(gerritFacade.trimResponse(response)).isEqualTo("{}");
    }

    @Test
    public void shouldParseListFiles() throws IOException, URISyntaxException, GerritPluginException {
        // given
        when(gerritConnectorMock.listFiles(PROJECT_NAME, BRANCH_NAME, CHANGE_ID, REVISION_ID)).thenReturn(
                LIST_FILES_RESPONSE);
        // when
        Map<String, String> files = gerritFacade.listFiles(PROJECT_NAME, BRANCH_NAME, CHANGE_ID, REVISION_ID);
        // then
        assertThat(files).hasSize(3);
        assertThat(files.get("src/main/java/com/google/gerrit/server/project/RefControl.java")).isEqualTo(
                "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java");
        assertThat(files.get("src/test/java/com/google/gerrit/server/project/RefControlTest.java")).isEqualTo(
                "gerrit-server/src/test/java/com/google/gerrit/server/project/RefControlTest.java");
        assertThat(files.get("src/com/google/gerrit/server/project/RefControl2.java")).isEqualTo(
                "gerrit-server/src/com/google/gerrit/server/project/RefControl2.java");
    }
}
