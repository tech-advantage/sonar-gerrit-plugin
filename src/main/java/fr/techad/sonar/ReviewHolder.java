package fr.techad.sonar;

import fr.techad.sonar.gerrit.review.ReviewInput;

public final class ReviewHolder {
    private static ReviewInput reviewInput = new ReviewInput();

    private ReviewHolder() {
    }

    public static ReviewInput getReviewInput() {
        return reviewInput;
    }
}
