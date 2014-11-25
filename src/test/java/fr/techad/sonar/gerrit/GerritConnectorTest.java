package fr.techad.sonar.gerrit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.resources.Project;

import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.gerrit.GerritConnector;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GerritConnectorTest {
    @Mock
    private Project projectMock;
    @Mock
    private DecoratorContext decoratorContextMock;
    @Mock
    private GerritConnector gerritConnector;

    @Test
    public void shouldAggregateBasicParamsWhenAuthenticated() throws GerritPluginException {
        // given
        gerritConnector = new GerritConnector("localhost", 80, "testuser", "testpass");
        // when
        // then
        assertThat(gerritConnector.rootUriBuilder("project", "branch", "changeid", "revisionid")).isEqualTo(
                "/a/changes/project~branch~changeid/revisions/revisionid");
    }

    @Test
    public void shouldEncodeBranchWithSlash() throws GerritPluginException {
        // given
        gerritConnector = new GerritConnector("localhost", 80, "testuser", "testpass");
        // when
        // then
        assertThat(gerritConnector.rootUriBuilder("project", "branch/subbranch", "changeid", "revisionid")).isEqualTo(
                "/a/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid");
    }

    @Test
    public void shouldPrependCustomBasePath() throws GerritPluginException {
        // given
        gerritConnector = new GerritConnector("https", "localhost", 443, "testuser", "testpass", "/r", "BASIC");
        // when
        // then
        assertThat(gerritConnector.rootUriBuilder("project", "branch/subbranch", "changeid", "revisionid")).isEqualTo(
                "/r/a/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid");
    }

    @Test
    public void shouldAggregateBasicParamsWhenAnonymous() throws GerritPluginException {
        // given
        gerritConnector = new GerritConnector("localhost", 80, "", "");
        // when
        // then
        assertThat(gerritConnector.rootUriBuilder("project", "branch", "changeid", "revisionid")).isEqualTo(
                "/changes/project~branch~changeid/revisions/revisionid");
    }

    @Test
    public void shouldPrependCustomBasePathWhenAnonymous() throws GerritPluginException {
        // given
        gerritConnector = new GerritConnector("https", "localhost", 443, "", "", "/r", "BASIC");
        // when
        // then
        assertThat(gerritConnector.rootUriBuilder("project", "branch/subbranch", "changeid", "revisionid")).isEqualTo(
                "/r/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid");
    }

    /**
     * @Test public void shouldAggregateBasicParamsWhenAnonymous() throws
     *       GerritPluginException { // given gerritConnector = new
     *       GerritConnector("server", 443, "", ""); // when // then
     *       assertThat(gerritConnector.rootUriBuilder("project", "branch",
     *       "changeid", "revisionid")).isEqualTo(
     *       "/changes/project~branch~changeid/revisions/revisionid"); }
     **/
}
