package fr.techad.sonar.gerrit.network.rest;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritConstants;
import fr.techad.sonar.GerritPluginException;
import fr.techad.sonar.PropertyKey;
import fr.techad.sonar.gerrit.GerritConnector;
import fr.techad.sonar.gerrit.factory.GerritConnectorFactory;
import fr.techad.sonar.mockito.MockitoExtension;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.sonar.api.config.internal.MapSettings;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GerritRestConnectorTest {
    static private String listFiles = ")]}'" +
        "  {" +
        "    \"/COMMIT_MSG\": {" +
        "      \"status\": \"A\"," +
        "      \"lines_inserted\": 7," +
        "      \"size_delta\": 551," +
        "      \"size\": 551" +
        "    }," +
        "    \"gerrit-server/src/main/java/com/google/gerrit/server/project/RefControl.java\": {" +
        "      \"status\": \"D\"," +
        "      \"lines_inserted\": 5," +
        "      \"lines_deleted\": 3," +
        "      \"size_delta\": 98," +
        "      \"size\": 23348" +
        "    }" +
        "  }";
    private MapSettings settings;
    private ClientAndServer mockServer;

    @BeforeAll
    public void startServer() {
        mockServer = startClientAndServer(10800);
    }

    @BeforeEach
    public void setUp() {
        // Common Settings
        settings = new MapSettings();
        settings.setProperty(PropertyKey.GERRIT_SCHEME, GerritConstants.SCHEME_HTTP)
            .setProperty(PropertyKey.GERRIT_HOST, "localhost")
            .appendProperty(PropertyKey.GERRIT_PORT, "10800")
            .setProperty(PropertyKey.GERRIT_PROJECT, "project")
            .setProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
            .setProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
            .setProperty(PropertyKey.GERRIT_LABEL, "Code-Review");
    }

    @AfterAll
    public void stopServer() {
        mockServer.stop();
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

    @Test
    public void shouldSetReview() throws IOException {
        mockServer.when(
            request()
                .withPath("/a/changes/project~branch~changeid/revisions/revisionid/review")
                .withMethod("POST"))
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
                    .withBody("{ message: 'Review committed' }")
                    .withDelay(TimeUnit.SECONDS, 1)
            );
        settings.setProperty(PropertyKey.GERRIT_USERNAME, "sonar")
            .setProperty(PropertyKey.GERRIT_PASSWORD, "sonar")
            .appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch");

        String response = getRestConnector().setReview("review");
        Assertions.assertEquals("{ message: 'Review committed' }", response);
    }

    @Test
    public void shouldSetReviewWithNullResponseBody() throws IOException {
        mockServer.when(
            request()
                .withPath("/a/changes/project~branch~changeid2/revisions/revisionid/review")
                .withMethod("POST"))
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
                    .withDelay(TimeUnit.SECONDS, 1)
            );
        settings.setProperty(PropertyKey.GERRIT_USERNAME, "sonar")
            .setProperty(PropertyKey.GERRIT_PASSWORD, "sonar")
            .appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch")
            .setProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid2")
        ;

        String response = getRestConnector().setReview("review");
        Assertions.assertEquals(StringUtils.EMPTY, response);
    }

    @Test
    public void shouldSetReviewAsAnonymous() throws IOException {
        mockServer.when(
            request()
                .withPath("/changes/project~branch~changeid/revisions/revisionid/review")
                .withMethod("POST"))
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
                    .withBody("{ message: 'Review committed' }")
                    .withDelay(TimeUnit.SECONDS, 1)
            );
        settings.setProperty(PropertyKey.GERRIT_BRANCH, "branch");

        String response = getRestConnector().setReview("review");
        Assertions.assertEquals("{ message: 'Review committed' }", response);
    }

    @Test
    public void shouldListFiles() throws IOException {
        mockServer.when(
            request()
                .withPath("/a/changes/project~branch~changeid/revisions/revisionid/files/")
                .withMethod("GET"))
            .respond(
                response()
                    .withStatusCode(200)
                    .withHeaders(
                        new Header("Content-Type", "application/json; charset=utf-8"),
                        new Header("Cache-Control", "public, max-age=86400"))
                    .withBody(listFiles)
                    .withDelay(TimeUnit.SECONDS, 1)
            );
        settings.setProperty(PropertyKey.GERRIT_USERNAME, "sonar")
            .setProperty(PropertyKey.GERRIT_PASSWORD, "sonar")
            .appendProperty(PropertyKey.GERRIT_BASE_PATH, "")
            .setProperty(PropertyKey.GERRIT_BRANCH, "branch");

        String response = getRestConnector().listFiles();
        Assertions.assertEquals(listFiles, response);
    }

    private GerritRestConnector getRestConnector() {
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritConnector gerritConnector = new GerritConnectorFactory(gerritConfiguration).getConnector();
        return (GerritRestConnector) gerritConnector;
    }
}
