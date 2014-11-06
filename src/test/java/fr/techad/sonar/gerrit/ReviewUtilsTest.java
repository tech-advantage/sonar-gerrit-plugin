package fr.techad.sonar.gerrit;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import fr.techad.sonar.gerrit.ReviewFileComment;
import fr.techad.sonar.gerrit.ReviewInput;
import fr.techad.sonar.gerrit.ReviewLineComment;
import fr.techad.sonar.gerrit.ReviewUtils;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReviewUtilsTest {

    ReviewInput reviewInput;
    ReviewLineComment rlcInfo;
    ReviewLineComment rlcCritical;
    List<ReviewFileComment> reviewList;

    @Before
    public void setUp() {
        reviewInput = new ReviewInput();

        reviewInput.setMessage("Not the default message.");

        rlcInfo = new ReviewLineComment();
        rlcInfo.setLine(12);
        rlcInfo.setMessage("INFO tldr");

        rlcCritical = new ReviewLineComment();
        rlcCritical.setLine(34);
        rlcCritical.setMessage("CRITICAL tldr");

        reviewList = new ArrayList<ReviewFileComment>(2);
        reviewList.add(rlcInfo);
        reviewList.add(rlcCritical);
    }

    @Test
    public void validateThresholdToValue() {
        // given
        // when
        // then
        assertThat(ReviewUtils.thresholdToValue("INFO")).isEqualTo(0);
        assertThat(ReviewUtils.thresholdToValue("MINOR")).isEqualTo(1);
        assertThat(ReviewUtils.thresholdToValue("MAJOR")).isEqualTo(2);
        assertThat(ReviewUtils.thresholdToValue("CRITICAL")).isEqualTo(3);
        assertThat(ReviewUtils.thresholdToValue("BLOCKER")).isEqualTo(4);
        assertThat(ReviewUtils.thresholdToValue("NOOP")).isEqualTo(-1);
    }

    @Test
    public void validateValueToThreshold() {
        // given
        // when
        // then
        assertThat(ReviewUtils.valueToThreshold(0)).isEqualTo("INFO");
        assertThat(ReviewUtils.valueToThreshold(1)).isEqualTo("MINOR");
        assertThat(ReviewUtils.valueToThreshold(2)).isEqualTo("MAJOR");
        assertThat(ReviewUtils.valueToThreshold(3)).isEqualTo("CRITICAL");
        assertThat(ReviewUtils.valueToThreshold(4)).isEqualTo("BLOCKER");
        assertThat(ReviewUtils.valueToThreshold(42)).isEqualTo("UNKNOWN");
        assertThat(ReviewUtils.valueToThreshold(-1)).isEqualTo("UNKNOWN");
    }

    @Test
    public void detectFilledReviewInput() {
        // given
        assertThat(reviewList.size() > 0);
        // when
        reviewInput.addComments("TLDR", reviewList);
        // then
        assertThat(ReviewUtils.isEmpty(reviewInput)).isFalse();
    }

    @Test
    public void detectEmptyReviewInput() {
        // given
        assertThat(reviewList.size() > 0);
        // when
        reviewInput.addComments("TLDR", reviewList);
        reviewInput.emptyComments();
        // then
        assertThat(ReviewUtils.isEmpty(reviewInput)).isTrue();
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
        assertThat(ReviewUtils.maxLevel(reviewInput)).isEqualTo(ReviewUtils.thresholdToValue("INFO"));
    }

    @Test
    public void detectCriticalLevel() {
        // given
        reviewInput.emptyComments();
        reviewList.add(rlcInfo);
        reviewList.add(rlcCritical);
        // when
        reviewInput.addComments("TLDR", reviewList);
        // then
        assertThat(ReviewUtils.maxLevel(reviewInput)).isEqualTo(ReviewUtils.thresholdToValue("CRITICAL"));
    }
}
