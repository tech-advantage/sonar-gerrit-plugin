package fr.techad.sonar.gerrit.network.ssh;

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
public class GerritSshFacadeTest {
    private static String COMMON_RESPONSE = "\"project\":\"sonar-gerrit-plugin\",\"branch\":\"feature/test\",\"id\":\"I218fb47cc01cbca2809dc82d54de019a60\",\"number\":11782,\"subject\":\"To develop\",";
    @Mock
    private GerritConnector gerritConnector;

    @Test
    @DisplayName("Should return a list files")
    public void shouldGetListFiles() throws IOException, GerritPluginException {
        String response = "{" + COMMON_RESPONSE +
            " \"currentPatchSet\": {" +
            "\"files\": [" +
            "{ " +
            "\"file\": \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\"," +
            "\"type\": \"ADDED\"" +
            "}," +
            "{ " +
            "\"file\": \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl2.java\"," +
            "\"type\": \"ADDED\"" +
            "}" +
            "]" +
            "}" +
            "}";
        when(gerritConnector.listFiles()).thenReturn(response);

        GerritSshFacade gerritSshFacade = new GerritSshFacade(gerritConnector);
        // Will call fillListFilesFromGerrit
        List<String> listFiles = gerritSshFacade.listFiles();
        Assertions.assertEquals(2, listFiles.size());
        Assertions.assertEquals("gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java", listFiles.get(0));
        Assertions.assertEquals("gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl2.java", listFiles.get(1));
    }

    @Test
    @DisplayName("Should return a list files with a defined type")
    public void shouldGetListFilesWithType() throws IOException, GerritPluginException {
        String response = "{" + COMMON_RESPONSE +
            " \"currentPatchSet\": {" +
            "\"files\": [" +
            "{ " +
            "\"file\": \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\"," +
            "\"type\": \"ADDED\"" +
            "}" +
            "]" +
            "}" +
            "}";
        when(gerritConnector.listFiles()).thenReturn(response);

        GerritSshFacade gerritSshFacade = new GerritSshFacade(gerritConnector);
        // Will call fillListFilesFromGerrit
        List<String> listFiles = gerritSshFacade.listFiles();
        Assertions.assertEquals(1, listFiles.size());
        Assertions.assertEquals("gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java", listFiles.get(0));
    }

    @Test
    @DisplayName("Should ignored deleted file")
    public void shouldIgnoredDeletedFiles() throws IOException, GerritPluginException {
        String response = "{" + COMMON_RESPONSE +
            " \"currentPatchSet\": {" +
            "\"files\": [" +
            "{ " +
            "\"file\": \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\"," +
            "\"type\": \"DELETED\"" +
            "}" +
            "]" +
            "}" +
            "}";
        when(gerritConnector.listFiles()).thenReturn(response);

        GerritSshFacade gerritSshFacade = new GerritSshFacade(gerritConnector);
        // Will call fillListFilesFromGerrit
        List<String> listFiles = gerritSshFacade.listFiles();
        Assertions.assertEquals(0, listFiles.size());
    }

    @Test
    @DisplayName("Should return an empty list if the review doesn't contain file")
    public void shouldReturnEmptyListIfReviewIsEmpty() throws IOException, GerritPluginException {
        String response = "{" + COMMON_RESPONSE +
            " \"currentPatchSet\": {" +
            "\"files\": [" +
            "]" +
            "}" +
            "}";

        when(gerritConnector.listFiles()).thenReturn(response);

        GerritSshFacade gerritSshFacade = new GerritSshFacade(gerritConnector);
        // Will call fillListFilesFromGerrit
        List<String> listFiles = gerritSshFacade.listFiles();
        Assertions.assertEquals(0, listFiles.size());
    }

    @Test
    @DisplayName("Should throw an exception on IOException")
    public void shouldThrowAnGerritException() {
        Assertions.assertThrows(GerritPluginException.class, () -> {
            when(gerritConnector.listFiles()).thenThrow(new IOException("Error during Test"));

            GerritSshFacade gerritRestFacade = new GerritSshFacade(gerritConnector);
            // Will call fillListFilesFromGerrit
            gerritRestFacade.listFiles();
        });
    }
}
