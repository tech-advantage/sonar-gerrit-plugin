package fr.techad.sonar;

import fr.techad.sonar.gerrit.ReviewInput;

public final class ReviewHolder {
    private static ReviewInput reviewInput = new ReviewInput();

    private ReviewHolder() {
    }

    public static ReviewInput getReviewInput() {
        return reviewInput;
    }
}
