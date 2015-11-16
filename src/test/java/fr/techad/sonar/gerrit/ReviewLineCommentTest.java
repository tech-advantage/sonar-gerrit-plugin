package fr.techad.sonar.gerrit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.ReviewLineComment;

@RunWith(MockitoJUnitRunner.class)
public class ReviewLineCommentTest {
	ReviewLineComment reviewLineComment;

	@Before
	public void setUp() {
		reviewLineComment = new ReviewLineComment();
	}

	@Test
	public void shouldHandleNullLine() throws GerritPluginException {
		// given
		// when
		reviewLineComment.setLine(null);
		// then
		assertThat(reviewLineComment.getLine()).isEqualTo(0);
	}

	@Test
	public void shouldHandleNegativeLine() throws GerritPluginException {
		// given
		// when
		reviewLineComment.setLine(-42);
		// then
		assertThat(reviewLineComment.getLine()).isEqualTo(0);
	}

	@Test
	public void shouldHandleZeroLine() throws GerritPluginException {
		// given
		// when
		reviewLineComment.setLine(0);
		// then
		assertThat(reviewLineComment.getLine()).isEqualTo(0);
	}
}
