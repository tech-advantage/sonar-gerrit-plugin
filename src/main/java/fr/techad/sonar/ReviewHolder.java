package fr.techad.sonar;

import fr.techad.sonar.gerrit.ReviewInput;

public class ReviewHolder {
    private static ReviewInput reviewInput = new ReviewInput();

    private ReviewHolder() {
    }

    public static ReviewInput getReviewInput() {
        return reviewInput;
    }
}
