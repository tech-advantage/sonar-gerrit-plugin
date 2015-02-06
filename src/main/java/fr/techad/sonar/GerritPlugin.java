package fr.techad.sonar;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.rule.Severity;

import fr.techad.sonar.gerrit.GerritConnector;
import fr.techad.sonar.gerrit.GerritFacade;

public final class GerritPlugin extends SonarPlugin {
    private static final String GERRIT_CATEGORY = "Gerrit";
    private static final String GERRIT_SUBCATEGORY_SERVER = "Server";
    private static final String GERRIT_SUBCATEGORY_REVIEW = "Review";
    private static final String GERRIT_ENABLED_DEFAULT = "true";
    private static final String GERRIT_FORCE_BRANCH_DEFAULT = "false";
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final String AUTH_BASIC = "basic";
    private static final String AUTH_DIGEST = "digest";
    private static final String GERRIT_COMMENT_NEW_ISSUES_ONLY = "false";
    private int serverBaseIndex;
    private int reviewBaseIndex;

    @Override
    public List<Object> getExtensions() {
        PropertyDefinition enabled = PropertyDefinition.builder(PropertyKey.GERRIT_ENABLED).category(GERRIT_CATEGORY)
                .subCategory(GERRIT_SUBCATEGORY_SERVER).type(PropertyType.BOOLEAN).defaultValue(GERRIT_ENABLED_DEFAULT)
                .onQualifiers(Arrays.asList(Qualifiers.PROJECT)).index(serverBaseIndex++).build();

        PropertyDefinition scheme = PropertyDefinition.builder(PropertyKey.GERRIT_SCHEME).category(GERRIT_CATEGORY)
                .subCategory(GERRIT_SUBCATEGORY_SERVER).type(PropertyType.SINGLE_SELECT_LIST)
                .options(SCHEME_HTTP, SCHEME_HTTPS).defaultValue(SCHEME_HTTP).index(serverBaseIndex++).build();

        PropertyDefinition host = PropertyDefinition.builder(PropertyKey.GERRIT_HOST).category(GERRIT_CATEGORY)
                .subCategory(GERRIT_SUBCATEGORY_SERVER).index(serverBaseIndex++).build();

        PropertyDefinition port = PropertyDefinition.builder(PropertyKey.GERRIT_HTTP_PORT).category(GERRIT_CATEGORY)
                .subCategory(GERRIT_SUBCATEGORY_SERVER).type(PropertyType.INTEGER).defaultValue("80")
                .index(serverBaseIndex++).build();

        PropertyDefinition username = PropertyDefinition.builder(PropertyKey.GERRIT_HTTP_USERNAME)
                .category(GERRIT_CATEGORY).subCategory(GERRIT_SUBCATEGORY_SERVER).index(serverBaseIndex++).build();

        PropertyDefinition password = PropertyDefinition.builder(PropertyKey.GERRIT_HTTP_PASSWORD)
                .category(GERRIT_CATEGORY).subCategory(GERRIT_SUBCATEGORY_SERVER).type(PropertyType.PASSWORD)
                .index(serverBaseIndex++).build();

        PropertyDefinition authScheme = PropertyDefinition.builder(PropertyKey.GERRIT_HTTP_AUTH_SCHEME)
                .category(GERRIT_CATEGORY).subCategory(GERRIT_SUBCATEGORY_SERVER).type(PropertyType.SINGLE_SELECT_LIST)
                .options(AUTH_BASIC, AUTH_DIGEST).defaultValue(AUTH_DIGEST).index(serverBaseIndex++).build();

        PropertyDefinition basePath = PropertyDefinition.builder(PropertyKey.GERRIT_BASE_PATH)
                .category(GERRIT_CATEGORY).subCategory(GERRIT_SUBCATEGORY_SERVER).defaultValue("/")
                .index(serverBaseIndex++).build();

        PropertyDefinition label = PropertyDefinition.builder(PropertyKey.GERRIT_LABEL).category(GERRIT_CATEGORY)
                .subCategory(GERRIT_SUBCATEGORY_REVIEW).defaultValue("Code-Review").index(reviewBaseIndex++).build();

        PropertyDefinition message = PropertyDefinition.builder(PropertyKey.GERRIT_MESSAGE).category(GERRIT_CATEGORY)
                .subCategory(GERRIT_SUBCATEGORY_REVIEW).defaultValue("Sonar review at ${sonar.host.url}")
                .index(reviewBaseIndex++).build();

        PropertyDefinition threshold = PropertyDefinition.builder(PropertyKey.GERRIT_THRESHOLD)
                .category(GERRIT_CATEGORY).subCategory(GERRIT_SUBCATEGORY_REVIEW).type(PropertyType.SINGLE_SELECT_LIST)
                .options(Severity.ALL).defaultValue(Severity.INFO).onQualifiers(Arrays.asList(Qualifiers.PROJECT))
                .index(reviewBaseIndex++).build();

        PropertyDefinition forceBranch = PropertyDefinition.builder(PropertyKey.GERRIT_FORCE_BRANCH)
                .category(GERRIT_CATEGORY).subCategory(GERRIT_SUBCATEGORY_REVIEW).type(PropertyType.BOOLEAN)
                .defaultValue(GERRIT_FORCE_BRANCH_DEFAULT).index(reviewBaseIndex++).build();

        PropertyDefinition newIssuesOnly = PropertyDefinition.builder(PropertyKey.GERRIT_COMMENT_NEW_ISSUES_ONLY)
                .category(GERRIT_CATEGORY).subCategory(GERRIT_SUBCATEGORY_REVIEW).type(PropertyType.BOOLEAN)
                .defaultValue(GERRIT_COMMENT_NEW_ISSUES_ONLY).onQualifiers(Arrays.asList(Qualifiers.PROJECT))
                .index(reviewBaseIndex++).build();

        return Arrays.asList(GerritConfiguration.class, GerritConnector.class, GerritFacade.class,
                GerritInitializer.class, GerritDecorator.class, GerritPostJob.class, enabled, scheme, host, port,
                username, password, authScheme, basePath, label, message, threshold, forceBranch, newIssuesOnly);
    }
}
