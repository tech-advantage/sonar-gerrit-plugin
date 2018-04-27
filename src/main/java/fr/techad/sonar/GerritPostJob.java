package fr.techad.sonar;

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.factory.GerritFacadeFactory;
import fr.techad.sonar.gerrit.review.ReviewFileComment;
import fr.techad.sonar.gerrit.review.ReviewInput;
import fr.techad.sonar.gerrit.review.ReviewLineComment;
import fr.techad.sonar.gerrit.utils.ReviewUtils;
import fr.techad.sonar.utils.MessageUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputPath;
import org.sonar.api.batch.postjob.PostJob;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GerritPostJob implements PostJob {
    private static final Logger LOG = Loggers.get(GerritPostJob.class);
    private final Settings settings;
    private final GerritConfiguration gerritConfiguration;
    private List<String> gerritModifiedFiles;
    private GerritFacade gerritFacade;
    private ReviewUtils reviewUtils;
    private MessageUtils messageUtils;
    private ReviewInput reviewInput = ReviewHolder.getReviewInput();

    public GerritPostJob(Settings settings, GerritConfiguration gerritConfiguration,
                         GerritFacadeFactory gerritFacadeFactory, ReviewUtils reviewUtils, MessageUtils messageUtils) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritPostJob");
        this.settings = settings;
        this.gerritFacade = gerritFacadeFactory.getFacade();
        this.gerritConfiguration = gerritConfiguration;
        this.reviewUtils = reviewUtils;
        this.messageUtils = messageUtils;
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
            reviewInput.setMessage(messageUtils.createMessage(gerritConfiguration.getMessage(), settings));

            LOG.debug("[GERRIT PLUGIN] Define message : {}", reviewInput.getMessage());
            LOG.debug("[GERRIT PLUGIN] Number of comments : {}", reviewInput.size());

            int maxLevel = reviewInput.maxLevelSeverity();
            LOG.debug("[GERRIT PLUGIN] Configured threshold {}, max review level {}",
                gerritConfiguration.getThreshold(), reviewUtils.valueToThreshold(maxLevel));

            if (reviewInput.isEmpty()) {
                LOG.debug("[GERRIT PLUGIN] No issues ! Vote {} for the label : {}",
                    gerritConfiguration.getVoteNoIssue(), gerritConfiguration.getLabel());
                reviewInput.setValueAndLabel(gerritConfiguration.getVoteNoIssue(), gerritConfiguration.getLabel());
            } else if (maxLevel < reviewUtils.thresholdToValue(gerritConfiguration.getThreshold())) {
                LOG.debug("[GERRIT PLUGIN] Issues below threshold. Vote {} for the label : {}",
                    gerritConfiguration.getVoteBelowThreshold(), gerritConfiguration.getLabel());
                reviewInput.setValueAndLabel(gerritConfiguration.getVoteBelowThreshold(),
                    gerritConfiguration.getLabel());
            } else {
                LOG.debug("[GERRIT PLUGIN] Issues above threshold. Vote {} for the label : {}",
                    gerritConfiguration.getVoteAboveThreshold(), gerritConfiguration.getLabel());
                reviewInput.setValueAndLabel(gerritConfiguration.getVoteAboveThreshold(),
                    gerritConfiguration.getLabel());
            }

            LOG.debug("[GERRIT PLUGIN] Send review for ChangeId={}, RevisionId={}", gerritConfiguration.getChangeId(),
                gerritConfiguration.getRevisionId());

            gerritFacade.setReview(reviewInput);

        } catch (GerritPluginException e) {
            LOG.error("[GERRIT PLUGIN] Error sending review to Gerrit", e);
        }
    }

    protected void decorate(InputPath resource, PostJobContext context, Collection<PostJobIssue> issues) {
        LOG.debug("[GERRIT PLUGIN] Decorate: {}", resource.relativePath());
        if (!resource.file().isFile()) {
            LOG.debug("[GERRIT PLUGIN] {} is not a file", resource.relativePath());
            return;
        }

        try {
            LOG.debug("[GERRIT PLUGIN] Start Sonar decoration for Gerrit");
            assertOrFetchGerritModifiedFiles();
        } catch (GerritPluginException e) {
            LOG.error("[GERRIT PLUGIN] Error getting Gerrit datas", e);
        }

        LOG.debug("[GERRIT PLUGIN] Look for in Gerrit if the file was under review, resource={}", resource);
        LOG.debug("[GERRIT PLUGIN] Look for in Gerrit if the file was under review, name={}", resource.relativePath());
        LOG.debug("[GERRIT PLUGIN] Look for in Gerrit if the file was under review, key={}", resource.key());

        String filename = getFileNameFromInputPath(resource);
        if (filename != null) {
            LOG.info("[GERRIT PLUGIN] Found a match between Sonar and Gerrit for {}: ", resource.relativePath(),
                filename);
            processFileResource(filename, issues);
        }
    }

    protected void assertOrFetchGerritModifiedFiles() throws GerritPluginException {
        if (gerritModifiedFiles != null) {
            return;
        }
        gerritModifiedFiles = gerritFacade.listFiles();
        LOG.debug("[GERRIT PLUGIN] Modified files in gerrit : {}", gerritModifiedFiles);
    }

    protected ReviewLineComment issueToComment(PostJobIssue issue) {
        ReviewLineComment result = new ReviewLineComment();

        result.setLine(issue.line());
        result.setSeverity(reviewUtils.thresholdToValue(issue.severity().toString()));

        result.setMessage(messageUtils.createIssueMessage(gerritConfiguration.getIssueComment(), settings, issue));
        LOG.debug("[GERRIT PLUGIN] issueToComment {}", result.toString());
        return result;
    }

    protected void processFileResource(@NotNull String file, @NotNull Collection<PostJobIssue> issuable) {
        List<ReviewFileComment> comments = new ArrayList<>();
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

    private String getFileNameFromInputPath(InputPath resource) {
        String filename = null;
        if (gerritModifiedFiles.contains(resource.relativePath())) {
            LOG.info("[GERRIT PLUGIN] Found a match between Sonar and Gerrit for {}", resource.relativePath());
            filename = resource.relativePath();
        } else if (gerritModifiedFiles.contains(gerritFacade.parseFileName(resource.relativePath()))) {
            LOG.info("[GERRIT PLUGIN] Found a match between Sonar and Gerrit for {}",
                gerritFacade.parseFileName(resource.relativePath()));
            filename = gerritFacade.parseFileName(resource.relativePath());
        } else {
            LOG.debug("[GERRIT PLUGIN] Parse the Gerrit List to look for the resource: {}", resource.relativePath());
            // Loop on each item
            for (String fileGerrit : gerritModifiedFiles) {
                if (gerritFacade.parseFileName(fileGerrit).equals(resource.relativePath())) {
                    filename = fileGerrit;
                    break;
                }
            }
        }
        if (filename == null) {
            LOG.debug("[GERRIT PLUGIN] File '{}' was not found in the review list)", resource.relativePath());
            LOG.debug("[GERRIT PLUGIN] Try to find with: '{}', '{}' and '{}'", resource.relativePath(),
                gerritFacade.parseFileName(resource.relativePath()));
        }
        return filename;
    }
}
