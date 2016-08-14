package fr.techad.sonar.gerrit.utils;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import fr.techad.sonar.gerrit.review.ReviewFileComment;

public final class ReviewUtils {
    private static final Logger LOG = Loggers.get(ReviewUtils.class);
    private static final String UNKNOWN = "UNKNOWN";

    private ReviewUtils() {
        super();
    }

    public static int thresholdToValue(String threshold) {
        int thresholdValue = ReviewFileComment.UNKNOWN_VALUE;

        if (StringUtils.equals(threshold, Severity.INFO)) {
            thresholdValue = ReviewFileComment.INFO_VALUE;
        } else if (StringUtils.equals(threshold, Severity.MINOR)) {
            thresholdValue = ReviewFileComment.MINOR_VALUE;
        } else if (StringUtils.equals(threshold, Severity.MAJOR)) {
            thresholdValue = ReviewFileComment.MAJOR_VALUE;
        } else if (StringUtils.equals(threshold, Severity.CRITICAL)) {
            thresholdValue = ReviewFileComment.CRITICAL_VALUE;
        } else if (StringUtils.equals(threshold, Severity.BLOCKER)) {
            thresholdValue = ReviewFileComment.BLOCKER_VALUE;
        } else {
            thresholdValue = ReviewFileComment.UNKNOWN_VALUE;
        }

        LOG.debug("[GERRIT PLUGIN] {} is converted to {}", threshold, thresholdValue);

        return thresholdValue;
    }

    public static String valueToThreshold(int value) {
        String threshold = UNKNOWN;

        switch (value) {
        case ReviewFileComment.INFO_VALUE:
            threshold = Severity.INFO;
            break;
        case ReviewFileComment.MINOR_VALUE:
            threshold = Severity.MINOR;
            break;
        case ReviewFileComment.MAJOR_VALUE:
            threshold = Severity.MAJOR;
            break;
        case ReviewFileComment.CRITICAL_VALUE:
            threshold = Severity.CRITICAL;
            break;
        case ReviewFileComment.BLOCKER_VALUE:
            threshold = Severity.BLOCKER;
            break;

        default:
            break;
        }
        LOG.debug("[GERRIT PLUGIN] {} is converted to {}", value, threshold);

        return threshold;
    }
}
