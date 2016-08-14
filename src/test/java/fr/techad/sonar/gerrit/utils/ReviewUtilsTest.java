package fr.techad.sonar.gerrit.utils;

import static org.fest.assertions.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
        rlcInfo.setSeverity(ReviewFileComment.INFO_VALUE);

        rlcCritical = new ReviewLineComment();
        rlcCritical.setLine(34);
        rlcCritical.setMessage("CRITI tldr");
        rlcCritical.setSeverity(ReviewFileComment.CRITICAL_VALUE);

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
        assertThat(reviewUtilsUnderTest.thresholdToValue("INFO")).isEqualTo(ReviewFileComment.INFO_VALUE);
        assertThat(reviewUtilsUnderTest.thresholdToValue("MINOR")).isEqualTo(ReviewFileComment.MINOR_VALUE);
        assertThat(reviewUtilsUnderTest.thresholdToValue("MAJOR")).isEqualTo(ReviewFileComment.MAJOR_VALUE);
        assertThat(reviewUtilsUnderTest.thresholdToValue("CRITICAL")).isEqualTo(ReviewFileComment.CRITICAL_VALUE);
        assertThat(reviewUtilsUnderTest.thresholdToValue("BLOCKER")).isEqualTo(ReviewFileComment.BLOCKER_VALUE);
        assertThat(reviewUtilsUnderTest.thresholdToValue("NOOP")).isEqualTo(ReviewFileComment.UNKNOWN_VALUE);
    }

    @Test
    public void validateValueToThreshold() {
        // given
        // when
        // then
        assertThat(reviewUtilsUnderTest.valueToThreshold(0)).isEqualTo("INFO");
        assertThat(reviewUtilsUnderTest.valueToThreshold(1)).isEqualTo("MINOR");
        assertThat(reviewUtilsUnderTest.valueToThreshold(2)).isEqualTo("MAJOR");
        assertThat(reviewUtilsUnderTest.valueToThreshold(3)).isEqualTo("CRITICAL");
        assertThat(reviewUtilsUnderTest.valueToThreshold(4)).isEqualTo("BLOCKER");
        assertThat(reviewUtilsUnderTest.valueToThreshold(42)).isEqualTo("UNKNOWN");
        assertThat(reviewUtilsUnderTest.valueToThreshold(-1)).isEqualTo("UNKNOWN");
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
