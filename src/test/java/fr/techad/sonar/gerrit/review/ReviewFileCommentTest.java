package fr.techad.sonar.gerrit.review;

import org.junit.Test;

import org.junit.Assert;

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
        reviewFileComment.setSeverity(ReviewFileComment.BLOCKER_VALUE);
        Assert.assertEquals(ReviewFileComment.BLOCKER_VALUE, reviewFileComment.getSeverity());
    }

    @Test
    public void testToString() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(MESSAGE);
        reviewFileComment.setSeverity(ReviewFileComment.BLOCKER_VALUE);
        Assert.assertEquals(
                "ReviewFileComment [message:" + MESSAGE + ", severity: " + ReviewFileComment.BLOCKER_VALUE + "]",
                reviewFileComment.toString());
    }

}
