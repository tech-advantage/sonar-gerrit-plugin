package fr.techad.sonar.gerrit.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.rule.Severity;

import fr.techad.sonar.gerrit.review.ReviewFileComment;
import fr.techad.sonar.gerrit.review.ReviewInput;
import fr.techad.sonar.gerrit.review.ReviewLineComment;

@RunWith(MockitoJUnitRunner.class)
public class ReviewUtilsTest {

    private ReviewInput reviewInput;
    private ReviewLineComment rlcInfo;
    private ReviewLineComment rlcCritical;
    private List<ReviewFileComment> reviewList;
    
    private ReviewUtils reviewUtilsUnderTest;

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
        
        reviewUtilsUnderTest = new ReviewUtils();
    }

    @Test
    public void validateThresholdToValue() {
        // given
        // when
        // then
        assertThat(reviewUtilsUnderTest.thresholdToValue("INFO")).isEqualTo(Severity.INFO.ordinal());
        assertThat(reviewUtilsUnderTest.thresholdToValue("MINOR")).isEqualTo(Severity.MINOR.ordinal());
        assertThat(reviewUtilsUnderTest.thresholdToValue("MAJOR")).isEqualTo(Severity.MAJOR.ordinal());
        assertThat(reviewUtilsUnderTest.thresholdToValue("CRITICAL")).isEqualTo(Severity.CRITICAL.ordinal());
        assertThat(reviewUtilsUnderTest.thresholdToValue("BLOCKER")).isEqualTo(Severity.BLOCKER.ordinal());
        assertThat(reviewUtilsUnderTest.thresholdToValue("NOOP")).isEqualTo(ReviewUtils.UNKNOWN_VALUE);
    }

    @Test
    public void validateValueToThreshold() {
        // given
        // when
        // then
        assertThat(reviewUtilsUnderTest.valueToThreshold(Severity.INFO.ordinal())).isEqualTo("INFO");
        assertThat(reviewUtilsUnderTest.valueToThreshold(Severity.MINOR.ordinal())).isEqualTo("MINOR");
        assertThat(reviewUtilsUnderTest.valueToThreshold(Severity.MAJOR.ordinal())).isEqualTo("MAJOR");
        assertThat(reviewUtilsUnderTest.valueToThreshold(Severity.CRITICAL.ordinal())).isEqualTo("CRITICAL");
        assertThat(reviewUtilsUnderTest.valueToThreshold(Severity.BLOCKER.ordinal())).isEqualTo("BLOCKER");
        assertThat(reviewUtilsUnderTest.valueToThreshold(42)).isEqualTo(ReviewUtils.UNKNOWN);
        assertThat(reviewUtilsUnderTest.valueToThreshold(-1)).isEqualTo(ReviewUtils.UNKNOWN);
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
        assertThat(reviewInput.maxLevelSeverity()).isEqualTo(reviewUtilsUnderTest.thresholdToValue("INFO"));
    }

    @Test
    public void detectCriticalLevel() {
        // given
        // when
        reviewInput.addComments("TLDR", reviewList);
        // then
        assertThat(reviewInput.maxLevelSeverity()).isEqualTo(reviewUtilsUnderTest.thresholdToValue("CRITICAL"));
    }
}
