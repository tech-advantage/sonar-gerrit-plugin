package fr.techad.sonar.gerrit.review;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.mockito.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class ReviewLineCommentTest {
    static final private String MESSAGE = "Gerrit Message";
    static final private Integer DEFAULT_LINE = new Integer(0);
    static final private Integer LINE = 42;
    static final private Integer NEGATIVE_LINE = -42;
    private ReviewLineComment reviewLineComment;

    @BeforeEach
    public void setUp() {
        reviewLineComment = new ReviewLineComment();
    }

    @Test
    public void shouldHandleLine() throws GerritPluginException {
        // given
        // when
        reviewLineComment.setLine(LINE);
        reviewLineComment.setMessage(MESSAGE);
        // then
        assertThat(reviewLineComment.getLine()).isEqualTo(42);
        assertThat(reviewLineComment.toString())
            .isEqualTo("ReviewLineComment [line=" + LINE + ", message=" + MESSAGE + "]");
    }

    @Test
    public void shouldHandleNullLine() throws GerritPluginException {
        // given
        // when
        reviewLineComment.setLine(null);
        // then
        assertThat(reviewLineComment.getLine()).isEqualTo(DEFAULT_LINE);
    }

    @Test
    public void shouldHandleNegativeLine() throws GerritPluginException {
        // given
        // when
        reviewLineComment.setLine(NEGATIVE_LINE);
        // then
        assertThat(reviewLineComment.getLine()).isEqualTo(DEFAULT_LINE);
    }

    @Test
    public void shouldHandleZeroLine() throws GerritPluginException {
        // given
        // when
        reviewLineComment.setLine(DEFAULT_LINE);
        // then
        assertThat(reviewLineComment.getLine()).isEqualTo(DEFAULT_LINE);
    }

}
