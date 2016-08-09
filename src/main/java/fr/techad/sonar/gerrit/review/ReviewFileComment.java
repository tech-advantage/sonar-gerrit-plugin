package fr.techad.sonar.gerrit.review;

import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Gerrit comment used with request for review input. Used with JSON marshaller
 * only.
 */
public class ReviewFileComment {
    private static final Logger LOG = Loggers.get(ReviewFileComment.class);

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(@NotNull String message) {
        LOG.debug("[GERRIT PLUGIN] ReviewFileComment setMessage {}", message);
        this.message = message;
    }

    @Override
    public String toString() {
        return "ReviewFileComment [message=" + message + "]";
    }
}
