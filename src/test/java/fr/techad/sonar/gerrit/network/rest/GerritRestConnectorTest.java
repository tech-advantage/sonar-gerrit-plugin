package fr.techad.sonar.gerrit.network.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.config.Settings;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritConstants;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.PropertyKey;
import fr.techad.sonar.gerrit.GerritConnector;
import fr.techad.sonar.gerrit.factory.GerritConnectorFactory;

@RunWith(MockitoJUnitRunner.class)
public class GerritRestConnectorTest {
    private Settings settings;

    @Before
    public void setUp() {
        // Common Settings
        settings = new Settings().appendProperty(PropertyKey.GERRIT_SCHEME, GerritConstants.SCHEME_HTTP)
                .appendProperty(PropertyKey.GERRIT_HOST, "localhost").appendProperty(PropertyKey.GERRIT_PORT, "8080")
                .appendProperty(PropertyKey.GERRIT_PROJECT, "project")
                .appendProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
                .appendProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
                .appendProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
    }

    @Test
    public void shouldAggregateBasicParamsWhenAuthenticated() throws GerritPluginException {
        // given
        settings.appendProperty(PropertyKey.GERRIT_USERNAME, "sonar")
                .appendProperty(PropertyKey.GERRIT_PASSWORD, "sonar").appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch");

        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/a/changes/project~branch~changeid/revisions/revisionid", gerritRestConnector.rootUriBuilder());
    }

    @Test
    public void shouldEncodeBranchWithSlash() throws GerritPluginException {
        // given
        settings.appendProperty(PropertyKey.GERRIT_USERNAME, "sonar")
                .appendProperty(PropertyKey.GERRIT_PASSWORD, "sonar").appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch");

        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/a/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid",
                gerritRestConnector.rootUriBuilder());
    }

    @Test
    public void shouldPrependCustomBasePath() throws GerritPluginException {
        // given
        settings.appendProperty(PropertyKey.GERRIT_USERNAME, "sonar")
                .appendProperty(PropertyKey.GERRIT_PASSWORD, "sonar").appendProperty(PropertyKey.GERRIT_BASE_PATH, "/r")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch");

        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/r/a/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid",
                gerritRestConnector.rootUriBuilder());
    }

    @Test
    public void shouldAggregateBasicParamsWhenAnonymous() throws GerritPluginException {
        // given
        settings.appendProperty(PropertyKey.GERRIT_USERNAME, "").appendProperty(PropertyKey.GERRIT_PASSWORD, "")
                .appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch");
        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid",
                gerritRestConnector.rootUriBuilder());
    }

    @Test
    public void shouldPrependCustomBasePathWhenAnonymous() throws GerritPluginException {
        // given
        settings.appendProperty(PropertyKey.GERRIT_USERNAME, "").appendProperty(PropertyKey.GERRIT_PASSWORD, "")
                .appendProperty(PropertyKey.GERRIT_BASE_PATH, "/r")
                .appendProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch");
        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/r/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid",
                gerritRestConnector.rootUriBuilder());
    }

    private GerritRestConnector getRestConnector() {
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritConnector gerritConnector = new GerritConnectorFactory(gerritConfiguration).getConnector();
        return (GerritRestConnector) gerritConnector;
    }
}
