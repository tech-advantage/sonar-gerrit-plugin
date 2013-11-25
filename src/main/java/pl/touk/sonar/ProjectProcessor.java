package pl.touk.sonar;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.resources.Resource;
import org.sonar.api.resources.ResourceUtils;
import org.sonar.api.rules.Violation;
import org.sonar.api.violations.ViolationQuery;
import pl.touk.sonar.gerrit.GerritFacade;
import pl.touk.sonar.gerrit.ReviewComment;
import pl.touk.sonar.gerrit.ReviewInput;

import java.util.ArrayList;
import java.util.List;

public class ProjectProcessor {
    private final static Logger LOG = LoggerFactory.getLogger(ProjectProcessor.class);
    private Review review;
    private ReviewInput reviewInput;
    private GerritFacade gerritFacade;
    private List<String> gerritModifiedFiles;

    public ProjectProcessor(@NotNull Review review) {
        this.review = review;
    }

    public void process() {
        try {
            review.validateGerritSettings();
            reviewInput = new ReviewInput();
            gerritFacade = new GerritFacade(review.getGerritHost(), review.getGerritPort(), review.getGerritUsername(), review.getGerritPassword());

            gerritModifiedFiles = gerritFacade.listFiles(review.getGerritChangeId(), review.getGerritRevisionId());
            processDecoratorContext(review.getContext());

            reviewInput.setLabelToPlusOne();
            gerritFacade.setReview(review.getGerritChangeId(), review.getGerritRevisionId(), reviewInput);
        } catch (GerritPluginException e) {
            LOG.error("Error processing project {} with Gerrit Plugin.", review.getProject().getName(), e);
        }
    }


    /** Recursive iteration of all elements. Couldn't find better solution for now. */
    private void processDecoratorContext(DecoratorContext context) {
        Resource resource = context.getResource();
        if (ResourceUtils.isFile(resource)) {
            processFileResource(context, resource);
        }
        for (DecoratorContext child : context.getChildren()) {
            processDecoratorContext(child);
        }
    }

    private void processFileResource(@NotNull DecoratorContext context, @NotNull Resource resource) {
        LOG.info("Processing resource scope {}, long name {}, name {}", new Object[] {resource.getScope(), resource.getLongName(), resource.getName()});
        if (gerritModifiedFiles.contains(resource.getLongName())) {
            List<ReviewComment> comments = new ArrayList<ReviewComment>();
            for(Violation violation : context.getViolations()) {
                LOG.info("Violation found: {}", violation.toString());
                comments.add(violationToComment(violation));
            }
            reviewInput.comments.put(resource.getLongName(), comments);
        }
    }

    protected ReviewComment violationToComment(Violation violation) {
        ReviewComment result = new ReviewComment();
        result.line = violation.getLineId();
        result.message = violation.getMessage();
        return result;
    }
}
