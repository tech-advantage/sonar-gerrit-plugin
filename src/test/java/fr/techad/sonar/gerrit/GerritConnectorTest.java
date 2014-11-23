package fr.techad.sonar.gerrit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.PropertyKey;
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
    @Mock
    private GerritConfiguration gerritConfiguration;

    private Settings settings;

    @Before
    public void setUp() {
        settings = new Settings().appendProperty(PropertyKey.GERRIT_SCHEME, "http")
                .appendProperty(PropertyKey.GERRIT_HOST, "localhost")
                .appendProperty(PropertyKey.GERRIT_HTTP_PORT, "8080")
                .appendProperty(PropertyKey.GERRIT_HTTP_USERNAME, "sonar")
                .appendProperty(PropertyKey.GERRIT_HTTP_PASSWORD, "sonar")
                .appendProperty(PropertyKey.GERRIT_BASE_PATH, "").appendProperty(PropertyKey.GERRIT_PROJECT, "project")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch")
                .appendProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
                .appendProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
                .appendProperty(PropertyKey.GERRIT_LABEL, "Code-Review");

        gerritConfiguration = new GerritConfiguration(settings);
    }

    @Test
    public void shouldAggregateBasicParamsWhenAuthenticated() throws GerritPluginException {
        // given
        // when
        gerritConnector = new GerritConnector(gerritConfiguration);
        // then
        assertThat(gerritConnector.rootUriBuilder()).isEqualTo(
                "/a/changes/project~branch~changeid/revisions/revisionid");
    }

    @Test
    public void shouldEncodeBranchWithSlash() throws GerritPluginException {
        // given
        settings = new Settings().appendProperty(PropertyKey.GERRIT_SCHEME, "http")
                .appendProperty(PropertyKey.GERRIT_HOST, "localhost")
                .appendProperty(PropertyKey.GERRIT_HTTP_PORT, "8080")
                .appendProperty(PropertyKey.GERRIT_HTTP_USERNAME, "sonar")
                .appendProperty(PropertyKey.GERRIT_HTTP_PASSWORD, "sonar")
                .appendProperty(PropertyKey.GERRIT_BASE_PATH, "").appendProperty(PropertyKey.GERRIT_PROJECT, "project")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch")
                .appendProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
                .appendProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
                .appendProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
        gerritConfiguration = new GerritConfiguration(settings);
        // when
        gerritConnector = new GerritConnector(gerritConfiguration);
        // then
        assertThat(gerritConnector.rootUriBuilder()).isEqualTo(
                "/a/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid");
    }

    @Test
    public void shouldPrependCustomBasePath() throws GerritPluginException {
        // given
        settings = new Settings().appendProperty(PropertyKey.GERRIT_SCHEME, "http")
                .appendProperty(PropertyKey.GERRIT_HOST, "localhost")
                .appendProperty(PropertyKey.GERRIT_HTTP_PORT, "8080")
                .appendProperty(PropertyKey.GERRIT_HTTP_USERNAME, "sonar")
                .appendProperty(PropertyKey.GERRIT_HTTP_PASSWORD, "sonar")
                .appendProperty(PropertyKey.GERRIT_BASE_PATH, "/r")
                .appendProperty(PropertyKey.GERRIT_PROJECT, "project")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch")
                .appendProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
                .appendProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
                .appendProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
        gerritConfiguration = new GerritConfiguration(settings);
        // when
        gerritConnector = new GerritConnector(gerritConfiguration);
        // then
        assertThat(gerritConnector.rootUriBuilder()).isEqualTo(
                "/r/a/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid");
    }

    @Test
    public void shouldAggregateBasicParamsWhenAnonymous() throws GerritPluginException {
        // given
        settings = new Settings().appendProperty(PropertyKey.GERRIT_SCHEME, "http")
                .appendProperty(PropertyKey.GERRIT_HOST, "localhost")
                .appendProperty(PropertyKey.GERRIT_HTTP_PORT, "8080")
                .appendProperty(PropertyKey.GERRIT_HTTP_USERNAME, "")
                .appendProperty(PropertyKey.GERRIT_HTTP_PASSWORD, "").appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
                .appendProperty(PropertyKey.GERRIT_PROJECT, "project")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch")
                .appendProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
                .appendProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
                .appendProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
        gerritConfiguration = new GerritConfiguration(settings);
        // when
        gerritConnector = new GerritConnector(gerritConfiguration);
        // then
        assertThat(gerritConnector.rootUriBuilder()).isEqualTo(
                "/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid");
    }

    @Test
    public void shouldPrependCustomBasePathWhenAnonymous() throws GerritPluginException {
        // given
        settings = new Settings().appendProperty(PropertyKey.GERRIT_SCHEME, "http")
                .appendProperty(PropertyKey.GERRIT_HOST, "localhost")
                .appendProperty(PropertyKey.GERRIT_HTTP_PORT, "8080")
                .appendProperty(PropertyKey.GERRIT_HTTP_USERNAME, "")
                .appendProperty(PropertyKey.GERRIT_HTTP_PASSWORD, "")
                .appendProperty(PropertyKey.GERRIT_BASE_PATH, "/r")
                .appendProperty(PropertyKey.GERRIT_PROJECT, "project")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch")
                .appendProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
                .appendProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
                .appendProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
        gerritConfiguration = new GerritConfiguration(settings);
        // when
        gerritConnector = new GerritConnector(gerritConfiguration);
        // then
        assertThat(gerritConnector.rootUriBuilder()).isEqualTo(
                "/r/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid");
    }
}
