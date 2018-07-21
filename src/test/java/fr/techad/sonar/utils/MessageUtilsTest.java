package fr.techad.sonar.utils;

import fr.techad.sonar.PropertyKey;
import org.junit.jupiter.api.Test;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleKey;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageUtilsTest {

    @Test
    public void validateSubstitution() {
        // given
        MapSettings settings = new MapSettings();
        settings.setProperty(PropertyKey.GERRIT_MESSAGE, "Sonar review at ${sonar.host.url}")
            .setProperty("sonar.host.url", "http://sq.example.com/");
        // then
        assertThat(MessageUtils.createMessage(settings.getString(PropertyKey.GERRIT_MESSAGE), settings))
            .isEqualTo("Sonar review at http://sq.example.com/");
    }

    @Test
    public void validateIssueSubstitution() {
        // given
        PostJobIssue issue = mock(PostJobIssue.class);
        when(issue.isNew()).thenReturn(true);
        when(issue.ruleKey()).thenReturn(RuleKey.of("squid", "XX12"));
        when(issue.message()).thenReturn("You have a problem there");
        when(issue.severity()).thenReturn(Severity.BLOCKER);
        // when
        MapSettings settings = new MapSettings();
        settings.setProperty(PropertyKey.GERRIT_ISSUE_COMMENT,
            "[${issue.isNew}] New: ${issue.ruleKey} on ${sonar.host.url} Severity: ${issue.severity}, Message: ${issue.message}")
            .setProperty("sonar.host.url", "http://sq.example.com/");
        // then
        assertThat(MessageUtils.createIssueMessage(settings.getString(PropertyKey.GERRIT_ISSUE_COMMENT), settings,
            issue)).isEqualTo(
            "[true] New: squid:XX12 on http://sq.example.com/ Severity: BLOCKER, Message: You have a problem there");
    }

}
