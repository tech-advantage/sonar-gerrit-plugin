package fr.techad.sonar;

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.GerritFacadeFactory;
import fr.techad.sonar.gerrit.ReviewFileComment;
import fr.techad.sonar.gerrit.ReviewInput;
import fr.techad.sonar.gerrit.ReviewLineComment;
import fr.techad.sonar.gerrit.ReviewUtils;

import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.DecoratorBarriers;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputPath;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Metric;
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
    private final Settings settings;
    private final GerritConfiguration gerritConfiguration;
    private List<String> gerritModifiedFiles;
    private GerritFacade gerritFacade;
    private ReviewInput reviewInput = ReviewHolder.getReviewInput();

    public GerritPostJob(Settings settings, GerritConfiguration gerritConfiguration,
            GerritFacadeFactory gerritFacadeFactory) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritPostJob");
        this.settings = settings;
        this.gerritFacade = gerritFacadeFactory.getFacade();
        this.gerritConfiguration = gerritConfiguration;
    }

    @Override
    public void describe(PostJobDescriptor descriptor) {
        descriptor.name("GERRIT PLUGIN");
        descriptor.requireProperty(PropertyKey.GERRIT_CHANGE_ID);
    }

    @Override
    public void execute(PostJobContext postJobContext) {
        if (!gerritConfiguration.isEnabled()) {
            LOG.info("[GERRIT PLUGIN] PostJob : analysis has finished. Plugin is disabled. No actions taken.");
            return;
        }

        Map<InputPath, List<PostJobIssue>> issueMap = new HashMap<>();
        for (PostJobIssue i : postJobContext.issues()) {
            InputComponent inputComponent = i.inputComponent();
            if (inputComponent instanceof InputPath) {
                InputPath inputPath = (InputPath) inputComponent;
                List<PostJobIssue> l = issueMap.get(inputPath);
                if (l == null) {
                    l = new ArrayList<>();
                    issueMap.put(inputPath, l);
                }
                l.add(i);
            }
        }

        for (Map.Entry<InputPath, List<PostJobIssue>> e : issueMap.entrySet()) {
            decorate(e.getKey(), postJobContext, e.getValue());
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

    public void decorate(InputPath resource, PostJobContext context, Collection<PostJobIssue> issues) {
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

        if (gerritModifiedFiles.contains(resource.relativePath())) {
            LOG.info("[GERRIT PLUGIN] Found a match between Sonar and Gerrit for {}", resource.relativePath());
            processFileResource(resource.relativePath(), issues);
        } else if (gerritModifiedFiles.contains(gerritFacade.parseFileName(resource.relativePath()))) {
            LOG.info("[GERRIT PLUGIN] Found a match between Sonar and Gerrit for {}",
                    gerritFacade.parseFileName(resource.relativePath()));
            processFileResource(gerritFacade.parseFileName(resource.relativePath()), issues);
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("[GERRIT PLUGIN] File is not under review ({} / {})", resource.relativePath(),
                        gerritFacade.parseFileName(resource.relativePath()));
            }
        }
    }

    protected void assertOrFetchGerritModifiedFiles() throws GerritPluginException {
        if (gerritModifiedFiles != null) {
            return;
        }
        gerritModifiedFiles = gerritFacade.listFiles();
        if (LOG.isDebugEnabled()) {
            LOG.debug("[GERRIT PLUGIN] Modified files in gerrit : {}", gerritModifiedFiles);
        }
    }

    protected ReviewLineComment issueToComment(PostJobIssue issue) {
        ReviewLineComment result = new ReviewLineComment();

        result.setLine(issue.line());

        result.setMessage(ReviewUtils.issueMessage(gerritConfiguration.getIssueComment(), settings, issue));
        if (LOG.isDebugEnabled()) {
            LOG.debug("[GERRIT PLUGIN] issueToComment {}", result.toString());
        }
        return result;
    }

    protected void processFileResource(@NotNull String file, @NotNull Collection<PostJobIssue> issuable) {
        List<ReviewFileComment> comments = new ArrayList<ReviewFileComment>();
        commentIssues(issuable, comments);
        if (!comments.isEmpty()) {
            reviewInput.addComments(file, comments);
        }
    }

    private void commentIssues(Collection<PostJobIssue> issues, List<ReviewFileComment> comments) {
        LOG.info("[GERRIT PLUGIN] Found {} issues", issues.size());

        for (PostJobIssue issue : issues) {
            if (gerritConfiguration.shouldCommentNewIssuesOnly() && !issue.isNew()) {
                LOG.info(
                        "[GERRIT PLUGIN] Issue is not new and only new one should be commented. Will not push back to Gerrit.");
            } else {
                comments.add(issueToComment(issue));
            }
        }
    }
}
