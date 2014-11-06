package fr.techad.sonar.gerrit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.ReviewFileComment;
import fr.techad.sonar.gerrit.ReviewInput;
import fr.techad.sonar.gerrit.ReviewLineComment;

@RunWith(MockitoJUnitRunner.class)
public class ReviewInputTest {
    private static final String COMPLETE_JSON = "{" + "\"message\":\"Not the default message.\","
            + "\"labels\":{\"Code-Review\":-1},"
            + "\"comments\":{\"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\":" + "["
            + "{\"message\":\"[nit] trailing whitespace\",\"line\":23},"
            + "{\"message\":\"[nit] s/conrtol/control\",\"line\":49}" + "]" + "}" + "}";

    private static final String REVIEWINPUT_JSON = "{" + "\"message\":\"Not the default message.\","
            + "\"labels\":{\"Code-Review\":-1}," + "\"comments\":{}" + "}";

    private static final String REVIEWLINECOMMENT_JSON = "{\"message\":\"[nit] trailing whitespace\",\"line\":23}";

    private static final String REVIEWLINECOMMENTLIST_JSON = "[{\"message\":\"[nit] trailing whitespace\",\"line\":23},"
            + "{\"message\":\"[nit] s/conrtol/control\",\"line\":49}]";

    private static final String COMMENTED_FILE = "gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java";

    ObjectMapper objectMapper;
    ReviewInput reviewInput;
    ReviewLineComment reviewLineComment;
    ReviewLineComment reviewLineComment2;
    List<ReviewFileComment> reviewList;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        reviewInput = new ReviewInput();

        reviewInput.setMessage("Not the default message.");
        reviewInput.setLabelToMinusOne("Code-Review");

        reviewLineComment = new ReviewLineComment();
        reviewLineComment.setLine(23);
        reviewLineComment.setMessage("[nit] trailing whitespace");

        reviewLineComment2 = new ReviewLineComment();
        reviewLineComment2.setLine(49);
        reviewLineComment2.setMessage("[nit] s/conrtol/control");

        reviewList = new ArrayList<ReviewFileComment>(2);
        reviewList.add(reviewLineComment);
        reviewList.add(reviewLineComment2);
    }

    @Test
    public void shouldJSONCompleteReviewInput() throws GerritPluginException, JsonProcessingException {
        // given
        reviewInput.emptyComments();
        reviewInput.addComments(COMMENTED_FILE, reviewList);
        // when
        String json = objectMapper.writeValueAsString(reviewInput);
        // then
        assertThat(json).isEqualTo(COMPLETE_JSON);
    }

    @Test
    public void shouldJSONReviewInput() throws GerritPluginException, JsonProcessingException {
        // given
        reviewInput.emptyComments();
        // when
        String json = objectMapper.writeValueAsString(reviewInput);
        // then
        assertThat(json).isEqualTo(REVIEWINPUT_JSON);
    }

    @Test
    public void shouldJSONReviewLineComment() throws GerritPluginException, JsonProcessingException {
        // given
        // when
        String json = objectMapper.writeValueAsString(reviewLineComment);
        // then
        assertThat(json).isEqualTo(REVIEWLINECOMMENT_JSON);
    }

    @Test
    public void shouldJSONReviewLineCommentList() throws GerritPluginException, JsonProcessingException {
        // given
        assertThat(reviewList.size()).isEqualTo(2);
        // when
        String json = objectMapper.writeValueAsString(reviewList);
        // then
        assertThat(json).isEqualTo(REVIEWLINECOMMENTLIST_JSON);
    }

}
