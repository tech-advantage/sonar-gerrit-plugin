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
        Assert.assertEquals("ReviewFileComment [message=" + MESSAGE + "]", reviewFileComment.toString());
    }

    @Test
    public void testNullMessage() {
        ReviewFileComment reviewFileComment = new ReviewFileComment();
        reviewFileComment.setMessage(null);
        Assert.assertEquals(null, reviewFileComment.getMessage());
    }

}
