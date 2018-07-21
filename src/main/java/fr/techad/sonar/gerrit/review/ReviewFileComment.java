package fr.techad.sonar.gerrit.review;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Objects;

/**
 * Gerrit comment used with request for review input. Used with JSON marshaller
 * only.
 */
public class ReviewFileComment {
    private static final Logger LOG = Loggers.get(ReviewFileComment.class);

    private String message;
    private int severity;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        LOG.debug("[GERRIT PLUGIN] ReviewFileComment setMessage {}", message);
        this.message = message;
        if (Objects.isNull(this.message))
            this.message = StringUtils.EMPTY;
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    @Override
    public String toString() {
        return "ReviewFileComment [" + "message:" + message + ", " + "severity: " + severity + "]";
    }
}
