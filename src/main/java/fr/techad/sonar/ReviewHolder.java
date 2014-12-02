package fr.techad.sonar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.techad.sonar.gerrit.ReviewInput;

public class ReviewHolder {
	private static final Logger LOG = LoggerFactory.getLogger(ReviewHolder.class);
	private static ReviewInput reviewInput = new ReviewInput();

	private ReviewHolder() {
	}

	public static ReviewInput getReviewInput() {
		LOG.debug("[GERRIT PLUGIN] Returning ReviewHolder.reviewInput");
		return reviewInput;
	}
}