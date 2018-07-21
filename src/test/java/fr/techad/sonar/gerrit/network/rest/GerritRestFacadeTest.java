package fr.techad.sonar.gerrit.network.rest;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.GerritConnector;
import fr.techad.sonar.mockito.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * TECH ADVANTAGE
 * All right reserved
 * Created by cochon on 21/07/2018.
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class GerritRestFacadeTest {

    @Mock
    private GerritConnector gerritConnector;

    @Test
    @DisplayName("Should return a list files")
    public void shouldGetListFiles() throws IOException, GerritPluginException {
        String response = ")]}'\n" +
            "  {\n" +
            "    \"/COMMIT_MSG\": {\n" +
            "      \"status\": \"A\",\n" +
            "      \"lines_inserted\": 7,\n" +
            "      \"size_delta\": 551,\n" +
            "      \"size\": 551\n" +
            "    },\n" +
            "    \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\": {\n" +
            "      \"lines_inserted\": 5,\n" +
            "      \"lines_deleted\": 3,\n" +
            "      \"size_delta\": 98,\n" +
            "      \"size\": 23348\n" +
            "    },\n" +
            "    \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl2.java\": {\n" +
            "      \"lines_inserted\": 5,\n" +
            "      \"lines_deleted\": 3,\n" +
            "      \"size_delta\": 98,\n" +
            "      \"size\": 23348\n" +
            "    }\n" +
            "  }";
        when(gerritConnector.listFiles()).thenReturn(response);

        GerritRestFacade gerritRestFacade = new GerritRestFacade(gerritConnector);
        // Will call fillListFilesFromGerrit
        List<String> listFiles = gerritRestFacade.listFiles();
        Assertions.assertEquals(2, listFiles.size());
        Assertions.assertEquals("gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java", listFiles.get(0));
        Assertions.assertEquals("gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl2.java", listFiles.get(1));
    }

    @Test
    @DisplayName("Should return a list files with a defined status")
    public void shouldGetListFilesWithStatus() throws IOException, GerritPluginException {
        String response = ")]}'\n" +
            "  {\n" +
            "    \"/COMMIT_MSG\": {\n" +
            "      \"status\": \"A\",\n" +
            "      \"lines_inserted\": 7,\n" +
            "      \"size_delta\": 551,\n" +
            "      \"size\": 551\n" +
            "    },\n" +
            "    \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\": {\n" +
            "      \"status\": \"A\",\n" +
            "      \"lines_inserted\": 5,\n" +
            "      \"lines_deleted\": 3,\n" +
            "      \"size_delta\": 98,\n" +
            "      \"size\": 23348\n" +
            "    }\n" +
            "  }";
        when(gerritConnector.listFiles()).thenReturn(response);

        GerritRestFacade gerritRestFacade = new GerritRestFacade(gerritConnector);
        // Will call fillListFilesFromGerrit
        List<String> listFiles = gerritRestFacade.listFiles();
        Assertions.assertEquals(1, listFiles.size());
        Assertions.assertEquals("gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java", listFiles.get(0));

    }

    @Test
    @DisplayName("Should ignored deleted file")
    public void shouldIgnoredDeletedFiles() throws IOException, GerritPluginException {
        String response = ")]}'\n" +
            "  {\n" +
            "    \"/COMMIT_MSG\": {\n" +
            "      \"status\": \"A\",\n" +
            "      \"lines_inserted\": 7,\n" +
            "      \"size_delta\": 551,\n" +
            "      \"size\": 551\n" +
            "    },\n" +
            "    \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\": {\n" +
            "      \"status\": \"D\",\n" +
            "      \"lines_inserted\": 5,\n" +
            "      \"lines_deleted\": 3,\n" +
            "      \"size_delta\": 98,\n" +
            "      \"size\": 23348\n" +
            "    }\n" +
            "  }";
        when(gerritConnector.listFiles()).thenReturn(response);

        GerritRestFacade gerritRestFacade = new GerritRestFacade(gerritConnector);
        // Will call fillListFilesFromGerrit
        List<String> listFiles = gerritRestFacade.listFiles();
        Assertions.assertEquals(0, listFiles.size());

    }

    @Test
    @DisplayName("Should return an empty list if the review doesn't contain file")
    public void shouldReturnEmptyListIfReviewIsEmpty() throws IOException, GerritPluginException {
        String response = ")]}'\n" +
            "  {\n" +
            "    \"/COMMIT_MSG\": {\n" +
            "      \"status\": \"A\",\n" +
            "      \"lines_inserted\": 7,\n" +
            "      \"size_delta\": 551,\n" +
            "      \"size\": 551\n" +
            "    }\n" +
            "  }";
        when(gerritConnector.listFiles()).thenReturn(response);

        GerritRestFacade gerritRestFacade = new GerritRestFacade(gerritConnector);
        // Will call fillListFilesFromGerrit
        List<String> listFiles = gerritRestFacade.listFiles();
        Assertions.assertEquals(0, listFiles.size());

    }

    @Test
    @DisplayName("Should throw an exception on IOException")
    public void shouldThrowAnGerritException() {
        Assertions.assertThrows(GerritPluginException.class, () -> {
            when(gerritConnector.listFiles()).thenThrow(new IOException("Error during Test"));

            GerritRestFacade gerritRestFacade = new GerritRestFacade(gerritConnector);
            // Will call fillListFilesFromGerrit
            gerritRestFacade.listFiles();
        });
    }
}
