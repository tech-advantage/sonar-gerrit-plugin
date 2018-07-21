package fr.techad.sonar.gerrit.review;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.rule.Severity;

public class ReviewFileCommentTest {

    static private final String MESSAGE = "Gerrit Message";

    @Test
    public void testMessage() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(MESSAGE);
        Assertions.assertEquals(MESSAGE, reviewFileComment.getMessage());
    }

    @Test
    public void testNullMessage() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(null);
        Assertions.assertEquals(StringUtils.EMPTY, reviewFileComment.getMessage());
    }

    @Test
    public void testSeverity() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(MESSAGE);
        reviewFileComment.setSeverity(Severity.BLOCKER.ordinal());
        Assertions.assertEquals(Severity.BLOCKER.ordinal(), reviewFileComment.getSeverity());
    }

    @Test
    public void testToString() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(MESSAGE);
        reviewFileComment.setSeverity(Severity.BLOCKER.ordinal());
        Assertions.assertEquals(
            "ReviewFileComment [message:" + MESSAGE + ", severity: " + Severity.BLOCKER.ordinal() + "]",
            reviewFileComment.toString());
    }

}
