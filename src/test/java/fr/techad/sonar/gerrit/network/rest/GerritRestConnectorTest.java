package fr.techad.sonar.gerrit.network.rest;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritConstants;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.PropertyKey;
import fr.techad.sonar.gerrit.GerritConnector;
import fr.techad.sonar.gerrit.factory.GerritConnectorFactory;
import fr.techad.sonar.mockito.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.sonar.api.config.internal.MapSettings;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class GerritRestConnectorTest {
    private MapSettings settings;

    @BeforeEach
    public void setUp() {
        // Common Settings
        settings = new MapSettings();
        settings.setProperty(PropertyKey.GERRIT_SCHEME, GerritConstants.SCHEME_HTTP)
            .setProperty(PropertyKey.GERRIT_HOST, "localhost")
            .appendProperty(PropertyKey.GERRIT_PORT, "8080")
            .setProperty(PropertyKey.GERRIT_PROJECT, "project")
            .setProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
            .setProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
            .setProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
    }

    @Test
    public void shouldAggregateBasicParamsWhenAuthenticated() throws GerritPluginException {
        // given
        settings.setProperty(PropertyKey.GERRIT_USERNAME, "sonar")
            .setProperty(PropertyKey.GERRIT_PASSWORD, "sonar")
            .appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch");

        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/a/changes/project~branch~changeid/revisions/revisionid",
            gerritRestConnector.rootUriBuilder());
    }

    @Test
    public void shouldEncodeBranchWithSlash() throws GerritPluginException {
        // given
        settings.setProperty(PropertyKey.GERRIT_USERNAME, "sonar")
            .setProperty(PropertyKey.GERRIT_PASSWORD, "sonar")
            .appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch");

        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/a/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid",
            gerritRestConnector.rootUriBuilder());
    }

    @Test
    public void shouldPrependCustomBasePath() throws GerritPluginException {
        // given
        settings.setProperty(PropertyKey.GERRIT_USERNAME, "sonar")
            .setProperty(PropertyKey.GERRIT_PASSWORD, "sonar")
            .appendProperty(PropertyKey.GERRIT_BASE_PATH, "/r")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch");

        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/r/a/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid",
            gerritRestConnector.rootUriBuilder());
    }

    @Test
    public void shouldAggregateBasicParamsWhenAnonymous() throws GerritPluginException {
        // given
        settings.setProperty(PropertyKey.GERRIT_USERNAME, "").appendProperty(PropertyKey.GERRIT_PASSWORD, "")
            .setProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch");
        // when
        GerritRestConnector gerritRestConnector = getRestConnector();

        // then
        assertEquals("/changes/project~branch%2Fsubbranch~changeid/revisions/revisionid",
            gerritRestConnector.rootUriBuilder());
    }

    @Test
    public void shouldPrependCustomBasePathWhenAnonymous() throws GerritPluginException {
        // given
        settings.setProperty(PropertyKey.GERRIT_USERNAME, "").appendProperty(PropertyKey.GERRIT_PASSWORD, "")
            .setProperty(PropertyKey.GERRIT_BASE_PATH, "/r")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch/subbranch");
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
