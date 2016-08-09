package fr.techad.sonar.gerrit.review;

import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class ReviewLineComment extends ReviewFileComment {
    private static final Logger LOG = Loggers.get(ReviewLineComment.class);
    private Integer line = 0;

    public Integer getLine() {
        return line;
    }

    public void setLine(@NotNull Integer setLine) {
        Integer tmpLine = 0;

        if (null == setLine) {
            LOG.debug("[GERRIT PLUGIN] ReviewLineComment line is null, forcing to 0");
        } else if (1 > setLine) {
            LOG.debug("[GERRIT PLUGIN] ReviewLineComment line < 0, forcing to 0");
        } else {
            LOG.debug("[GERRIT PLUGIN] ReviewLineComment setLine {}", setLine);
            tmpLine = setLine;
        }

        this.line = tmpLine;
    }

    @Override
    public String toString() {
        return "ReviewLineComment [line=" + line + ", message=" + getMessage() + "]";
    }
}
