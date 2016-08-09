package fr.techad.sonar.gerrit.network.rest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.GerritConstants;
import fr.techad.sonar.gerrit.GerritConnector;

public class GerritRestConnector implements GerritConnector {
    private static final Logger LOG = Loggers.get(GerritRestConnector.class);
    private static final String URI_AUTH_PREFIX = "/a";
    private static final String URI_CHANGES = "/changes/%s~%s~%s";
    private static final String URI_REVISIONS = "/revisions/%s";
    private static final String URI_LIST_FILES_SUFFIX = "/files/";
    private static final String URI_SET_REVIEW = "/review";
    private static int REQUEST_COUNTER;
    private HttpHost httpHost;
    private CloseableHttpClient httpClient;
    private HttpClientContext httpClientContext;
    private final GerritConfiguration gerritConfiguration;

    public GerritRestConnector(GerritConfiguration gerritConfiguration) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritConnector");
        this.gerritConfiguration = gerritConfiguration;
    }

    @NotNull
    @Override
    public String listFiles() throws IOException {
        String getUri = rootUriBuilder();
        getUri = getUri.concat(URI_LIST_FILES_SUFFIX);

        LOG.info("[GERRIT PLUGIN] Listing files from {}", getUri);

        HttpGet httpGet = new HttpGet(getUri);
        httpGet.addHeader("Accept", "application/json");
        CloseableHttpResponse httpResponse = logAndExecute(httpGet);
        return consumeAndLogEntity(httpResponse);
    }

    @NotNull
    @Override
    public String setReview(String reviewInputAsJson) throws IOException {
        LOG.info("[GERRIT PLUGIN] Setting review {}", reviewInputAsJson);

        String postUri = rootUriBuilder();
        postUri = postUri.concat(URI_SET_REVIEW);

        LOG.info("[GERRIT PLUGIN] Setting review at {}", postUri);

        HttpPost httpPost = new HttpPost(postUri);
        httpPost.setEntity(new StringEntity(reviewInputAsJson, ContentType.APPLICATION_JSON));

        CloseableHttpResponse httpResponse = logAndExecute(httpPost);
        return consumeAndLogEntity(httpResponse);
    }

    // Example
    // http://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientPreemptiveDigestAuthentication.java
    private void createHttpContext() {
        httpHost = new HttpHost(gerritConfiguration.getHost(), gerritConfiguration.getPort(),
                gerritConfiguration.getScheme());
        httpClientContext = HttpClientContext.create();

        if (gerritConfiguration.isAnonymous()) {
            httpClient = HttpClients.createDefault();
        } else {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(gerritConfiguration.getHost(), gerritConfiguration.getPort()),
                    new UsernamePasswordCredentials(gerritConfiguration.getUsername(),
                            gerritConfiguration.getPassword()));
            httpClient = HttpClients.custom().setDefaultCredentialsProvider(credentialsProvider).build();

            BasicAuthCache basicAuthCache = new BasicAuthCache();
            AuthScheme authScheme = null;

            if (GerritConstants.AUTH_BASIC.equalsIgnoreCase(gerritConfiguration.getHttpAuthScheme())) {
                authScheme = new BasicScheme();
            } else if (GerritConstants.AUTH_DIGEST.equalsIgnoreCase(gerritConfiguration.getHttpAuthScheme())) {
                authScheme = new DigestScheme();

            } else {
                LOG.error("[GERRIT PLUGIN] createHttpContext called with AUTH_SCHEME {} instead of digest or basic",
                        gerritConfiguration.getHttpAuthScheme());
            }
            basicAuthCache.put(httpHost, authScheme);
            httpClientContext.setAuthCache(basicAuthCache);
        }
    }

    @NotNull
    private CloseableHttpResponse logAndExecute(@NotNull HttpRequestBase request) throws IOException {
        if (null == httpClient || null == httpClientContext || null == httpHost) {
            createHttpContext();
        }

        LOG.info("[GERRIT PLUGIN] Request {}: {} to {}",
                new Object[] { ++REQUEST_COUNTER, request.getMethod(), request.getURI().toString() });
        CloseableHttpResponse httpResponse = httpClient.execute(httpHost, request, httpClientContext);
        LOG.info("[GERRIT PLUGIN] Response {}: {}", REQUEST_COUNTER, httpResponse.getStatusLine().toString());
        return httpResponse;
    }

    @NotNull
    private String consumeAndLogEntity(@NotNull CloseableHttpResponse response) throws IOException {
        if (response.getEntity() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] Entity {}: no entity", REQUEST_COUNTER);
            }
            return StringUtils.EMPTY;
        }
        String content = EntityUtils.toString(response.getEntity());
        LOG.info("[GERRIT PLUGIN] Entity {}: {}", REQUEST_COUNTER, content);
        return content;
    }

    @NotNull
    private String encode(String content) {
        String result = "";
        try {
            result = URLEncoder.encode(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("[GERRIT PLUGIN] Error during encodage", e);
        } catch (NullPointerException npe) {
            LOG.warn("[GERRIT PLUGIN] Could not encode. Is content empty ?");
        }
        return result;
    }

    @NotNull
    public String rootUriBuilder() {
        String basePath = gerritConfiguration.getBasePath();
        if ("/".compareTo(basePath) == 0) {
            basePath = "";
        }

        String uri = basePath;
        if (!gerritConfiguration.isAnonymous()) {
            uri = uri.concat(URI_AUTH_PREFIX);
        }
        uri = uri.concat(String.format(URI_CHANGES, encode(gerritConfiguration.getProjectName()),
                encode(gerritConfiguration.getBranchName()), encode(gerritConfiguration.getChangeId())));
        uri = uri.concat(String.format(URI_REVISIONS, encode(gerritConfiguration.getRevisionId())));

        if (LOG.isDebugEnabled()) {
            LOG.debug("[GERRIT PLUGIN] Built URI : {}", uri);
        }

        return uri;
    }
}
