package fr.techad.sonar;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.rule.Severity;

import fr.techad.sonar.gerrit.GerritConnectorFactory;
import fr.techad.sonar.gerrit.GerritFacadeFactory;

public final class GerritPlugin extends SonarPlugin {

	private int serverBaseIndex;
	private int reviewBaseIndex;

	@Override
	public List<Object> getExtensions() {
		PropertyDefinition enabled = PropertyDefinition.builder(PropertyKey.GERRIT_ENABLED)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.type(PropertyType.BOOLEAN).defaultValue(GerritConstants.GERRIT_ENABLED_DEFAULT)
				.onQualifiers(Arrays.asList(Qualifiers.PROJECT)).index(serverBaseIndex++).build();

		PropertyDefinition scheme = PropertyDefinition.builder(PropertyKey.GERRIT_SCHEME)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.type(PropertyType.SINGLE_SELECT_LIST)
				.options(GerritConstants.SCHEME_HTTP, GerritConstants.SCHEME_HTTPS, GerritConstants.SCHEME_SSH)
				.defaultValue(GerritConstants.SCHEME_HTTP).index(serverBaseIndex++).build();

		PropertyDefinition host = PropertyDefinition.builder(PropertyKey.GERRIT_HOST)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.index(serverBaseIndex++).build();

		PropertyDefinition port = PropertyDefinition.builder(PropertyKey.GERRIT_PORT)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.type(PropertyType.INTEGER).defaultValue("80").index(serverBaseIndex++).build();

		PropertyDefinition username = PropertyDefinition.builder(PropertyKey.GERRIT_USERNAME)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.index(serverBaseIndex++).build();

		PropertyDefinition password = PropertyDefinition.builder(PropertyKey.GERRIT_PASSWORD)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.type(PropertyType.PASSWORD).index(serverBaseIndex++).build();

		PropertyDefinition sshKeyPath = PropertyDefinition.builder(PropertyKey.GERRIT_SSH_KEY_PATH)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.type(PropertyType.STRING).index(serverBaseIndex++).build();

		PropertyDefinition authScheme = PropertyDefinition.builder(PropertyKey.GERRIT_HTTP_AUTH_SCHEME)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.type(PropertyType.SINGLE_SELECT_LIST).options(GerritConstants.AUTH_BASIC, GerritConstants.AUTH_DIGEST)
				.defaultValue(GerritConstants.AUTH_DIGEST).index(serverBaseIndex++).build();

		PropertyDefinition basePath = PropertyDefinition.builder(PropertyKey.GERRIT_BASE_PATH)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_SERVER)
				.defaultValue("/").index(serverBaseIndex++).build();

		PropertyDefinition label = PropertyDefinition.builder(PropertyKey.GERRIT_LABEL)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_REVIEW)
				.defaultValue("Code-Review").index(reviewBaseIndex++).build();

		PropertyDefinition message = PropertyDefinition.builder(PropertyKey.GERRIT_MESSAGE)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_REVIEW)
				.defaultValue("Sonar review at ${sonar.host.url}").index(reviewBaseIndex++).build();

		PropertyDefinition forceBranch = PropertyDefinition.builder(PropertyKey.GERRIT_FORCE_BRANCH)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_REVIEW)
				.type(PropertyType.BOOLEAN).defaultValue(GerritConstants.GERRIT_FORCE_BRANCH_DEFAULT)
				.index(reviewBaseIndex++).build();

		PropertyDefinition newIssuesOnly = PropertyDefinition.builder(PropertyKey.GERRIT_COMMENT_NEW_ISSUES_ONLY)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_REVIEW)
				.type(PropertyType.BOOLEAN).defaultValue(GerritConstants.GERRIT_COMMENT_NEW_ISSUES_ONLY)
				.onQualifiers(Arrays.asList(Qualifiers.PROJECT)).index(reviewBaseIndex++).build();

		PropertyDefinition threshold = PropertyDefinition.builder(PropertyKey.GERRIT_THRESHOLD)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_REVIEW)
				.type(PropertyType.SINGLE_SELECT_LIST).options(Severity.ALL).defaultValue(Severity.INFO)
				.onQualifiers(Arrays.asList(Qualifiers.PROJECT)).index(reviewBaseIndex++).build();

		PropertyDefinition voteNoIssue = PropertyDefinition.builder(PropertyKey.GERRIT_VOTE_NO_ISSUE)
				.category(GerritConstants.GERRIT_CATEGORY).subCategory(GerritConstants.GERRIT_SUBCATEGORY_REVIEW)
				.type(PropertyType.SINGLE_SELECT_LIST).options("+1", "+2")
				.defaultValue(GerritConstants.GERRIT_VOTE_NO_ISSUE_DEFAULT)
				.onQualifiers(Arrays.asList(Qualifiers.PROJECT)).index(reviewBaseIndex++).build();

		PropertyDefinition voteIssueBelowThreshold = PropertyDefinition
				.builder(PropertyKey.GERRIT_VOTE_ISSUE_BELOW_THRESHOLD).category(GerritConstants.GERRIT_CATEGORY)
				.subCategory(GerritConstants.GERRIT_SUBCATEGORY_REVIEW).type(PropertyType.SINGLE_SELECT_LIST)
				.options("-2", "-1", "0", "+1", "+2")
				.defaultValue(GerritConstants.GERRIT_VOTE_ISSUE_BELOW_THRESHOLD_DEFAULT)
				.onQualifiers(Arrays.asList(Qualifiers.PROJECT)).index(reviewBaseIndex++).build();

		PropertyDefinition voteIssueAboveThreshold = PropertyDefinition
				.builder(PropertyKey.GERRIT_VOTE_ISSUE_ABOVE_THRESHOLD).category(GerritConstants.GERRIT_CATEGORY)
				.subCategory(GerritConstants.GERRIT_SUBCATEGORY_REVIEW).type(PropertyType.SINGLE_SELECT_LIST)
				.options("-2", "-1", "0").defaultValue(GerritConstants.GERRIT_VOTE_ISSUE_ABOVE_THRESHOLD_DEFAULT)
				.onQualifiers(Arrays.asList(Qualifiers.PROJECT)).index(reviewBaseIndex++).build();

		return Arrays.asList(GerritConfiguration.class, GerritConnectorFactory.class, GerritFacadeFactory.class,
				GerritInitializer.class, GerritProjectBuilder.class, GerritPostJob.class, enabled, scheme, host, port,
				username, password, authScheme, basePath, sshKeyPath, label, message, forceBranch, newIssuesOnly,
				threshold, voteNoIssue, voteIssueBelowThreshold, voteIssueAboveThreshold);
	}
}
