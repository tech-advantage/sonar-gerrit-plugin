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
        gerritFacade = new GerritFacade(gerritConnectorMock);
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
        when(gerritConnectorMock.listFiles()).thenReturn(LIST_FILES_RESPONSE);
        // when
        Map<String, String> files = gerritFacade.listFiles();
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
