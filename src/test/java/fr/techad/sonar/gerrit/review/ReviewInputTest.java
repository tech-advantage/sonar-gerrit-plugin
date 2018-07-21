package fr.techad.sonar.gerrit.review;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReviewInputTest {
    private static final String DEFAULT_MESSAGE = "Looks good to me.";
    private static final String NEW_MESSAGE = "A new message";

    private static final String PLUS_ONE_LABEL = "+1 Label";
    private static final String MINUS_ONE_LABEL = "-1 Label";
    private static final String OTHER_LABEL = "other Label";

    private static final Integer PLUS_ONE = new Integer(1);
    private static final Integer MINUS_ONE = new Integer(-1);
    private static final Integer OTHER_VALUE = new Integer(42);

    private static final String KEY_COMMENT1 = "Key1";
    private static final String KEY_COMMENT2 = "Key2";

    @Test
    public void shouldHaveAMessage() {
        ReviewInput reviewInput = new ReviewInput();
        assertEquals(DEFAULT_MESSAGE, reviewInput.getMessage());
        reviewInput.setMessage(NEW_MESSAGE);
        assertEquals(NEW_MESSAGE, reviewInput.getMessage());
    }

    @Test
    public void shouldHaveAPlusOneLabel() {
        ReviewInput reviewInput = new ReviewInput();
        reviewInput.setLabelToPlusOne(PLUS_ONE_LABEL);
        Map<String, Integer> labels = reviewInput.getLabels();
        assertEquals(1, labels.size());
        assertEquals(PLUS_ONE, labels.get(PLUS_ONE_LABEL));

    }

    @Test
    public void shouldHaveAMinusOneLabel() {
        ReviewInput reviewInput = new ReviewInput();
        reviewInput.setLabelToMinusOne(MINUS_ONE_LABEL);
        Map<String, Integer> labels = reviewInput.getLabels();
        assertEquals(1, labels.size());
        assertEquals(MINUS_ONE, labels.get(MINUS_ONE_LABEL));

    }

    @Test
    public void shouldHaveAOtherLabel() {
        ReviewInput reviewInput = new ReviewInput();
        reviewInput.setValueAndLabel(OTHER_VALUE, OTHER_LABEL);
        Map<String, Integer> labels = reviewInput.getLabels();
        assertEquals(1, labels.size());
        assertEquals(OTHER_VALUE, labels.get(OTHER_LABEL));
    }

    @Test
    public void shouldHaveSeveralLabel() {
        ReviewInput reviewInput = new ReviewInput();
        reviewInput.setLabelToPlusOne(PLUS_ONE_LABEL);
        reviewInput.setValueAndLabel(OTHER_VALUE, OTHER_LABEL);
        reviewInput.setLabelToMinusOne(MINUS_ONE_LABEL);
        Map<String, Integer> labels = reviewInput.getLabels();
        assertEquals(3, labels.size());
        assertEquals(OTHER_VALUE, labels.get(OTHER_LABEL));
        assertEquals(PLUS_ONE, labels.get(PLUS_ONE_LABEL));
        assertEquals(MINUS_ONE, labels.get(MINUS_ONE_LABEL));
    }

    @Test
    public void shouldEmptyTheComments() {
        ReviewInput reviewInput = new ReviewInput();
        List<ReviewFileComment> list = new ArrayList<>();
        list.add(new ReviewFileComment());
        reviewInput.addComments(KEY_COMMENT1, list);
        assertEquals(1, reviewInput.size());
        reviewInput.emptyComments();
        assertEquals(0, reviewInput.size());
    }

    @Test
    public void shouldHaveTheComments() {
        ReviewInput reviewInput = new ReviewInput();
        List<ReviewFileComment> list1 = new ArrayList<>();
        list1.add(new ReviewFileComment());
        list1.add(new ReviewFileComment());
        reviewInput.addComments(KEY_COMMENT1, list1);
        List<ReviewFileComment> list2 = new ArrayList<>();
        list2.add(new ReviewFileComment());
        reviewInput.addComments(KEY_COMMENT2, list2);
        assertEquals(2, reviewInput.size());
        Map<String, List<ReviewFileComment>> comments = reviewInput.getComments();

        assertEquals(2, comments.get(KEY_COMMENT1).size());
        assertEquals(1, comments.get(KEY_COMMENT2).size());
    }

    @Test
    public void shouldHaveTheUnmodifiedComments() {
        ReviewInput reviewInput = new ReviewInput();
        List<ReviewFileComment> list1 = new ArrayList<>();
        list1.add(new ReviewFileComment());
        list1.add(new ReviewFileComment());
        reviewInput.addComments(KEY_COMMENT1, list1);
        list1.add(new ReviewFileComment());
        assertEquals(1, reviewInput.size());
        Map<String, List<ReviewFileComment>> comments = reviewInput.getComments();

        assertEquals(2, comments.get(KEY_COMMENT1).size());
    }

    @Test
    public void shouldHaveAToString() {
        ReviewInput reviewInput = new ReviewInput();
        reviewInput.setLabelToPlusOne(PLUS_ONE_LABEL);
        reviewInput.setMessage(NEW_MESSAGE);
        List<ReviewFileComment> list1 = new ArrayList<>();
        list1.add(new ReviewFileComment());
        assertEquals("ReviewInput [message=A new message, labels={+1 Label=1}, comments={}]", reviewInput.toString());
    }

    @Test
    public void shoulsHaveAEmptyReview() {
        ReviewInput reviewInput = new ReviewInput();
        reviewInput.emptyComments();
        assertThat(reviewInput.isEmpty()).isTrue();
    }

    @Test
    public void shouldHaveANoEmptyReview() {
        ReviewInput reviewInput = new ReviewInput();
        List<ReviewFileComment> list = new ArrayList<>();
        list.add(new ReviewFileComment());
        reviewInput.addComments(KEY_COMMENT1, list);
        assertThat(reviewInput.isEmpty()).isFalse();
    }

}
