package fr.techad.sonar.gerrit;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gerrit comment used with request for review input. Used with JSON marshaller
 * only.
 */
public class ReviewFileComment {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewFileComment.class);

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(@NotNull String message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("[GERRIT PLUGIN] ReviewFileComment setMessage {}", message);
        }
        this.message = message;
    }

    @Override
    public String toString() {
        return "ReviewFileComment [message=" + message + "]";
    }
}
