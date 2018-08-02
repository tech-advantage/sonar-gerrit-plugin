package fr.techad.sonar.gerrit;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.review.ReviewFileComment;
import fr.techad.sonar.gerrit.review.ReviewInput;
import fr.techad.sonar.gerrit.review.ReviewLineComment;
import fr.techad.sonar.mockito.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class GerritFacadeTest {

    @Mock
    GerritConnector gerritConnectorMock;

    @Captor
    ArgumentCaptor<String> argCaptor;

    @Test
    public void shouldCallOnceFillListFilesFromGerrit() throws GerritPluginException {
        GerritFacade gerritFacade = new GerritFacadeUT(gerritConnectorMock);
        // First call
        List<String> listFiles = gerritFacade.listFiles();
        Assertions.assertEquals(1, listFiles.size());
        Assertions.assertEquals("FileNameClass.java", listFiles.get(0));
        // Second call: should be ignored -> cnt=1
        gerritFacade.listFiles();
        Assertions.assertEquals(1, ((GerritFacadeUT) gerritFacade).getCnt());
    }

    @Test
    public void shouldSetReview() throws GerritPluginException, IOException {
        ReviewInput reviewInputMock = Mockito.mock(ReviewInput.class);
        // Mock the message
        when(reviewInputMock.getMessage()).thenReturn("Message Test");
        // Mock labels
        Map<String, Integer> labels = new HashMap<>();
        labels.put("Quality Control", 1);
        labels.put("Verify", 2);
        when(reviewInputMock.getLabels()).thenReturn(labels);
        // Mock comments
        ReviewLineComment reviewLineCommentMock = Mockito.mock(ReviewLineComment.class);
        when(reviewLineCommentMock.getLine()).thenReturn(1, 4, 6, 8).thenReturn(null);
        when(reviewLineCommentMock.getMessage()).thenReturn("Msg1", "Msg2", "Msg3", "Msg4").thenReturn(null);
        List<ReviewFileComment> reviewFileComments = new ArrayList<>();
        reviewFileComments.add(reviewLineCommentMock);
        reviewFileComments.add(reviewLineCommentMock);
        Map<String, List<ReviewFileComment>> comments = new HashMap<>();
        comments.put("FileTest1.java", reviewFileComments);
        comments.put("FileTest2.java", reviewFileComments);
        when(reviewInputMock.getComments()).thenReturn(comments);

        GerritFacade gerritFacade = new GerritFacadeUT(gerritConnectorMock);
        gerritFacade.setReview(reviewInputMock);

        Mockito.verify(gerritConnectorMock).setReview(argCaptor.capture());
        String captorValue = argCaptor.getValue();
        Assertions.assertEquals("{\"message\":\"Message Test\",\"labels\":{\"Verify\":2,\"Quality Control\":1},\"comments\":{\"FileTest2.java\":[{\"line\":1,\"message\":\"Msg1\"},{\"line\":4,\"message\":\"Msg2\"}],\"FileTest1.java\":[{\"line\":6,\"message\":\"Msg3\"},{\"line\":8,\"message\":\"Msg4\"}]}}", captorValue);
    }

    @Test
    public void shouldSetReviewOnlyMessage() throws GerritPluginException, IOException {
        ReviewInput reviewInputMock = Mockito.mock(ReviewInput.class);
        when(reviewInputMock.getMessage()).thenReturn("Message Test");

        GerritFacade gerritFacade = new GerritFacadeUT(gerritConnectorMock);
        gerritFacade.setReview(reviewInputMock);

        Mockito.verify(gerritConnectorMock).setReview(argCaptor.capture());
        String captorValue = argCaptor.getValue();
        Assertions.assertEquals("{\"message\":\"Message Test\"}", captorValue);
    }

    @Test
    public void shouldSetReviewWithNullMessage() throws GerritPluginException, IOException {

        ReviewInput reviewInputMock = Mockito.mock(ReviewInput.class);
        when(reviewInputMock.getMessage()).thenReturn(null);

        GerritFacade gerritFacade = new GerritFacadeUT(gerritConnectorMock);
        gerritFacade.setReview(reviewInputMock);

        Mockito.verify(gerritConnectorMock).setReview(argCaptor.capture());
        String captorValue = argCaptor.getValue();
        Assertions.assertEquals("{\"message\":null}", captorValue);
    }

    @Test
    public void shouldThrowException() {
        Assertions.assertThrows(GerritPluginException.class, () -> {
            ReviewInput reviewInputMock = Mockito.mock(ReviewInput.class);
            GerritFacade gerritFacade = new GerritFacadeUT(gerritConnectorMock);
            when(gerritConnectorMock.setReview(Mockito.any())).thenThrow(new IOException("Test"));
            gerritFacade.setReview(reviewInputMock);
        });
    }

    @Test
    public void shouldGetGerritConnector() {
        GerritFacade gerritFacade = new GerritFacadeUT(gerritConnectorMock);
        Assertions.assertEquals(gerritConnectorMock, gerritFacade.getGerritConnector());
    }

    @Test
    public void testParseFileName() {
        GerritFacade facade = Mockito.mock(GerritFacade.class, Mockito.CALLS_REAL_METHODS);
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

    class GerritFacadeUT extends GerritFacade {
        private int cnt = 0;

        public GerritFacadeUT(GerritConnector gerritConnector) {
            super(gerritConnector);
        }

        @Override
        protected void fillListFilesFromGerrit() throws GerritPluginException {
            addFile("FileNameClass.java");
            addFile("/COMMIT_MSG"); // Should be ignored because it's the commit message "key"
            cnt++;
        }

        public int getCnt() {
            return cnt;
        }
    }

}
