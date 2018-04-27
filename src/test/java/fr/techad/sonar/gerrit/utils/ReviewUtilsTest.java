package fr.techad.sonar.gerrit.utils;

import fr.techad.sonar.gerrit.review.ReviewFileComment;
import fr.techad.sonar.gerrit.review.ReviewInput;
import fr.techad.sonar.gerrit.review.ReviewLineComment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.rule.Severity;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReviewUtilsTest {

    private ReviewInput reviewInput;
    private ReviewLineComment rlcInfo;
    private ReviewLineComment rlcCritical;
    private List<ReviewFileComment> reviewList;

    @Before
    public void setUp() {
        reviewInput = new ReviewInput();

        reviewInput.setMessage("Not the default message.");

        rlcInfo = new ReviewLineComment();
        rlcInfo.setLine(12);
        rlcInfo.setMessage("INFORMATION tldr");
        rlcInfo.setSeverity(Severity.INFO.ordinal());

        rlcCritical = new ReviewLineComment();
        rlcCritical.setLine(34);
        rlcCritical.setMessage("CRITI tldr");
        rlcCritical.setSeverity(Severity.CRITICAL.ordinal());

        reviewList = new ArrayList<ReviewFileComment>(2);
        reviewList.add(rlcInfo);
        reviewList.add(rlcCritical);
    }

    @Test
    public void validateThresholdToValue() {
        // given
        // when
        // then
        assertThat(ReviewUtils.thresholdToValue("INFO")).isEqualTo(Severity.INFO.ordinal());
        assertThat(ReviewUtils.thresholdToValue("MINOR")).isEqualTo(Severity.MINOR.ordinal());
        assertThat(ReviewUtils.thresholdToValue("MAJOR")).isEqualTo(Severity.MAJOR.ordinal());
        assertThat(ReviewUtils.thresholdToValue("CRITICAL")).isEqualTo(Severity.CRITICAL.ordinal());
        assertThat(ReviewUtils.thresholdToValue("BLOCKER")).isEqualTo(Severity.BLOCKER.ordinal());
        assertThat(ReviewUtils.thresholdToValue("NOOP")).isEqualTo(ReviewUtils.UNKNOWN_VALUE);
    }

    @Test
    public void validateValueToThreshold() {
        // given
        // when
        // then
        assertThat(ReviewUtils.valueToThreshold(Severity.INFO.ordinal())).isEqualTo("INFO");
        assertThat(ReviewUtils.valueToThreshold(Severity.MINOR.ordinal())).isEqualTo("MINOR");
        assertThat(ReviewUtils.valueToThreshold(Severity.MAJOR.ordinal())).isEqualTo("MAJOR");
        assertThat(ReviewUtils.valueToThreshold(Severity.CRITICAL.ordinal())).isEqualTo("CRITICAL");
        assertThat(ReviewUtils.valueToThreshold(Severity.BLOCKER.ordinal())).isEqualTo("BLOCKER");
        assertThat(ReviewUtils.valueToThreshold(42)).isEqualTo(ReviewUtils.UNKNOWN);
        assertThat(ReviewUtils.valueToThreshold(-1)).isEqualTo(ReviewUtils.UNKNOWN);
    }

    @Test
    public void detectInfoLevel() {
        // given
        reviewInput.emptyComments();
        reviewList.clear();
        reviewList.add(rlcInfo);
        // when
        reviewInput.addComments("TLDR", reviewList);
        // then
        assertThat(reviewInput.maxLevelSeverity()).isEqualTo(ReviewUtils.thresholdToValue("INFO"));
    }

    @Test
    public void detectCriticalLevel() {
        // given
        // when
        reviewInput.addComments("TLDR", reviewList);
        // then
        assertThat(reviewInput.maxLevelSeverity()).isEqualTo(ReviewUtils.thresholdToValue("CRITICAL"));
    }
}
