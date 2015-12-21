package fr.techad.sonar.gerrit;

import fr.techad.sonar.PropertyKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.postjob.issue.Issue;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReviewUtilsTest {
	@Mock
    private Settings settings;

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
    
    @Test
    public void validateSubstitution() {
    	// given
    	// when
    	settings = new Settings().appendProperty(PropertyKey.GERRIT_MESSAGE, "Sonar review at ${sonar.host.url}")
    			.appendProperty("sonar.host.url", "http://sq.example.com/");
    	// then
    	assertThat(ReviewUtils.substituteProperties(settings.getString(PropertyKey.GERRIT_MESSAGE), settings))
    	.isEqualTo("Sonar review at http://sq.example.com/");
    }

    @Test
    public void validateIssueSubstitution() {
        // given
        Issue issue = mock(Issue.class);
        when(issue.isNew()).thenReturn(true);
        when(issue.ruleKey()).thenReturn(RuleKey.of("squid", "XX12"));
        when(issue.message()).thenReturn("You have a problem there");
        when(issue.severity()).thenReturn(Severity.BLOCKER);
        // when
        settings = new Settings()
            .appendProperty(PropertyKey.GERRIT_ISSUE_COMMENT, "[${issue.isNew}] New: ${issue.ruleKey} on ${sonar.host.url} Severity: ${issue.severity}, Message: ${issue.message}")
            .appendProperty("sonar.host.url", "http://sq.example.com/");
        // then
        assertThat(ReviewUtils.issueMessage(settings.getString(PropertyKey.GERRIT_ISSUE_COMMENT), settings, issue))
            .isEqualTo("[true] New: squid:XX12 on http://sq.example.com/ Severity: BLOCKER, Message: You have a problem there");
    }
}
