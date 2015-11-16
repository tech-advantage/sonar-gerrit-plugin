package fr.techad.sonar;

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.GerritFacadeFactory;
import fr.techad.sonar.gerrit.ReviewFileComment;
import fr.techad.sonar.gerrit.ReviewInput;
import fr.techad.sonar.gerrit.ReviewLineComment;
import fr.techad.sonar.gerrit.ReviewUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.DecoratorBarriers;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.PostJob;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputPath;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.issue.Issue;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasuresFilters;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DependsUpon(DecoratorBarriers.ISSUES_TRACKED)
public class GerritPostJob implements PostJob {
	private static final Logger LOG = Loggers.get(GerritPostJob.class);
	private static final String ISSUE_FORMAT = "[%s] New: %s Severity: %s, Message: %s";
	private static final String ALERT_FORMAT = "[ALERT] Severity: %s, Message: %s";
	private final Settings settings;
	private final GerritConfiguration gerritConfiguration;
	private final PostJobContext postJobContext;
	private Map<String, String> gerritModifiedFiles;
	private GerritFacade gerritFacade;
	private ReviewInput reviewInput = ReviewHolder.getReviewInput();

	public GerritPostJob(Settings settings, GerritConfiguration gerritConfiguration,
			GerritFacadeFactory gerritFacadeFactory, PostJobContext postJobContext) {
		LOG.debug("[GERRIT PLUGIN] Instanciating GerritPostJob");
		this.settings = settings;
		this.gerritFacade = gerritFacadeFactory.getFacade();
		this.gerritConfiguration = gerritConfiguration;
		this.postJobContext = postJobContext;
	}

	@Override
	public void executeOn(Project project, SensorContext context) {
		if (!gerritConfiguration.isEnabled()) {
			LOG.info("[GERRIT PLUGIN] PostJob : analysis has finished. Plugin is disabled. No actions taken.");
			return;
		}

		Map<InputPath, List<Issue>> issueMap = new HashMap<>();
		for (Issue i : postJobContext.issues()) {
			InputComponent inputComponent = i.inputComponent();
			if (inputComponent instanceof InputPath) {
				InputPath inputPath = (InputPath) inputComponent;
				List<Issue> l = issueMap.get(inputPath);
				if (l == null) {
					l = new ArrayList<>();
					issueMap.put(inputPath, l);
				}
				l.add(i);
			}
		}

		for (Map.Entry<InputPath, List<Issue>> e : issueMap.entrySet()) {
			decorate(e.getKey(), context, e.getValue());
		}

		try {
			LOG.info("[GERRIT PLUGIN] Analysis has finished. Sending results to Gerrit.");
			reviewInput.setMessage(ReviewUtils.substituteProperties(gerritConfiguration.getMessage(), settings));

			if (LOG.isDebugEnabled()) {
				LOG.debug("[GERRIT PLUGIN] Define message : {}", reviewInput.getMessage());
				LOG.debug("[GERRIT PLUGIN] Number of comments : {}", reviewInput.size());
			}

			int maxLevel = ReviewUtils.maxLevel(reviewInput);
			if (LOG.isDebugEnabled()) {
				LOG.debug("[GERRIT PLUGIN] Configured threshold {}, max review level {}",
						gerritConfiguration.getThreshold(), ReviewUtils.valueToThreshold(maxLevel));
			}

			if (ReviewUtils.isEmpty(reviewInput)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("[GERRIT PLUGIN] No issues ! Vote {} for the label : {}",
							gerritConfiguration.getVoteNoIssue(), gerritConfiguration.getLabel());
				}
				reviewInput.setValueAndLabel(gerritConfiguration.getVoteNoIssue(), gerritConfiguration.getLabel());
			} else if (maxLevel < ReviewUtils.thresholdToValue(gerritConfiguration.getThreshold())) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("[GERRIT PLUGIN] Issues below threshold. Vote {} for the label : {}",
							gerritConfiguration.getVoteBelowThreshold(), gerritConfiguration.getLabel());
				}
				reviewInput.setValueAndLabel(gerritConfiguration.getVoteBelowThreshold(),
						gerritConfiguration.getLabel());
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("[GERRIT PLUGIN] Issues above threshold. Vote {} for the label : {}",
							gerritConfiguration.getVoteAboveThreshold(), gerritConfiguration.getLabel());
				}
				reviewInput.setValueAndLabel(gerritConfiguration.getVoteAboveThreshold(),
						gerritConfiguration.getLabel());
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("[GERRIT PLUGIN] Send review for ChangeId={}, RevisionId={}",
						gerritConfiguration.getChangeId(), gerritConfiguration.getRevisionId());
			}

			gerritFacade.setReview(reviewInput);

		} catch (GerritPluginException e) {
			LOG.error("[GERRIT PLUGIN] Error sending review to Gerrit", e);
		}
	}

	@DependsUpon
	public String dependsOnViolations() {
		return DecoratorBarriers.ISSUES_ADDED;
	}

	@DependsUpon
	public Metric<?> dependsOnAlerts() {
		return CoreMetrics.ALERT_STATUS;
	}

	public void decorate(InputPath resource, SensorContext context, Collection<Issue> issues) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("[GERRIT PLUGIN] Decorate: {}", resource.relativePath());
		}
		if (!resource.file().isFile()) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("[GERRIT PLUGIN] {} is not a file", resource.relativePath());
			}
			return;
		}

		try {
			LOG.debug("[GERRIT PLUGIN] Start Sonar decoration for Gerrit");
			assertOrFetchGerritModifiedFiles();
		} catch (GerritPluginException e) {
			LOG.error("[GERRIT PLUGIN] Error getting Gerrit datas", e);
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("[GERRIT PLUGIN] Look for in Gerrit if the file was under review, name={}",
					resource.relativePath());
		}
		if (gerritModifiedFiles.containsKey(resource.relativePath())) {
			LOG.info("[GERRIT PLUGIN] File in Sonar {} matches file in Gerrit {}", resource.relativePath(),
					gerritModifiedFiles.get(resource.relativePath()));
			processFileResource(resource, context, issues);
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("[GERRIT PLUGIN] File {} is not under review", resource.relativePath());
			}
		}
	}

	protected void assertOrFetchGerritModifiedFiles() throws GerritPluginException {
		if (gerritModifiedFiles != null) {
			return;
		}
		gerritModifiedFiles = gerritFacade.listFiles();
		if (LOG.isDebugEnabled()) {
			LOG.debug("[GERRIT PLUGIN] Modified files in gerrit (keys) : {}", gerritModifiedFiles.keySet());
			LOG.debug("[GERRIT PLUGIN] Modified files in gerrit (values): {}", gerritModifiedFiles.values());
		}
	}

	protected ReviewLineComment issueToComment(Issue issue) {
		ReviewLineComment result = new ReviewLineComment();

		result.setLine(issue.line());
		result.setMessage(String.format(ISSUE_FORMAT, issue.isNew(), StringUtils.capitalize(issue.ruleKey().toString()),
				issue.severity(), issue.message()));
		if (LOG.isDebugEnabled()) {
			LOG.debug("[GERRIT PLUGIN] issueToComment {}", result.toString());
		}
		return result;
	}

	protected ReviewFileComment measureToComment(Measure<?> measure) {
		ReviewFileComment result = new ReviewFileComment();
		result.setMessage(String.format(ALERT_FORMAT, measure.getAlertStatus().toString(), measure.getAlertText()));
		if (LOG.isDebugEnabled()) {
			LOG.debug("[GERRIT PLUGIN] measureToComment {}", result.toString());
		}

		return result;
	}

	protected void processFileResource(@NotNull InputPath resource, @NotNull SensorContext context,
			Collection<Issue> issuable) {
		List<ReviewFileComment> comments = new ArrayList<ReviewFileComment>();
		commentIssues(issuable, comments);
		commentAlerts(context, comments);
		if (!comments.isEmpty()) {
			reviewInput.addComments(gerritModifiedFiles.get(resource.relativePath()), comments);
		}
	}

	private void commentIssues(Collection<Issue> issues, List<ReviewFileComment> comments) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("[GERRIT PLUGIN] Found {} issues", issues.size());
		}
		for (Issue issue : issues) {
			LOG.info("[GERRIT PLUGIN] Issue found: {}", issue.toString());

			if (gerritConfiguration.shouldCommentNewIssuesOnly() && !issue.isNew()) {
				LOG.info(
						"[GERRIT PLUGIN] Issue is not new and only new one should be commented. Will not push back to Gerrit.");
				/*
				 * } else if (StringUtils.equals(issue.resolution(),
				 * Issue.RESOLUTION_FALSE_POSITIVE)) { LOG.info(
				 * "[GERRIT PLUGIN] Issue marked as false-positive. Will not push back to Gerrit."
				 * );
				 */
			} else {
				comments.add(issueToComment(issue));
			}
		}
	}

	private void commentAlerts(SensorContext context, List<ReviewFileComment> comments) {
		LOG.debug("[GERRIT PLUGIN] Found {} alerts", context.getMeasures(MeasuresFilters.all()).size());
		for (Measure<?> measure : context.getMeasures(MeasuresFilters.all())) {
			Metric.Level level = measure.getAlertStatus();
			if (level == null || level == Metric.Level.OK) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("[GERRIT PLUGIN] Alert level is {}. Continue.", level);
				}
				continue;
			}
			LOG.info("[GERRIT PLUGIN] Alert found: {}", level.toString());
			comments.add(measureToComment(measure));
		}
	}
}
