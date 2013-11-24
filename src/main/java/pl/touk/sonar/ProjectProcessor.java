package pl.touk.sonar;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectProcessor {
    private final static Logger LOG = LoggerFactory.getLogger(ProjectProcessor.class);
    private Review review;

    public ProjectProcessor(@NotNull Review review) {
        this.review = review;
    }

    public void process() {
        try {
            review.validateGerritSettings();

        } catch (GerritPluginException e) {
            LOG.error("Error processing project {} with Gerrit Plugin.", review.getProject().getName(), e);

        }
    }
}
