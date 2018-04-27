package fr.techad.sonar.gerrit.review;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.batch.rule.Severity;

public class ReviewFileCommentTest {

    static private final String MESSAGE = "Gerrit Message";

    @Test
    public void testMessage() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(MESSAGE);
        Assert.assertEquals(MESSAGE, reviewFileComment.getMessage());
    }

    @Test
    public void testNullMessage() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(null);
        Assert.assertEquals(null, reviewFileComment.getMessage());
    }

    @Test
    public void testSeverity() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(MESSAGE);
        reviewFileComment.setSeverity(Severity.BLOCKER.ordinal());
        Assert.assertEquals(Severity.BLOCKER.ordinal(), reviewFileComment.getSeverity());
    }

    @Test
    public void testToString() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(MESSAGE);
        reviewFileComment.setSeverity(Severity.BLOCKER.ordinal());
        Assert.assertEquals(
            "ReviewFileComment [message:" + MESSAGE + ", severity: " + Severity.BLOCKER.ordinal() + "]",
            reviewFileComment.toString());
    }

}
