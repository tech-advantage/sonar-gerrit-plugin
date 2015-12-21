package fr.techad.sonar.gerrit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.sonar.api.batch.postjob.issue.Issue;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.Severity;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class ReviewUtils {
    private static final Logger LOG = Loggers.get(ReviewUtils.class);
    private static final String LOG_MESSAGE = "[GERRIT PLUGIN] Got review level {}, level is now {}";
    private static final String UNKNOWN = "UNKNOWN";

    private static final String PROP_START = "${";
    private static final int PROP_START_LENGTH = PROP_START.length();
    private static final char PROP_END = '}';

    private static final int INFO_VALUE = 0;
    private static final int MINOR_VALUE = 1;
    private static final int MAJOR_VALUE = 2;
    private static final int CRITICAL_VALUE = 3;
    private static final int BLOCKER_VALUE = 4;
    private static final int UNKNOWN_VALUE = -1;

    private static final String ISSUE_PREFIX   = "issue";
    private static final String ISSUE_IS_NEW   = "isNew";
    private static final String ISSUE_RULE_KEY = "ruleKey";
    private static final String ISSUE_SEVERITY = "severity";
    private static final String ISSUE_MESSAGE  = "message";

    private ReviewUtils() {

    }

    public static boolean isEmpty(ReviewInput ri) {
        return ri.getComments().isEmpty();
    }

    public static int thresholdToValue(String threshold) {
        int thresholdValue = UNKNOWN_VALUE;

        if (StringUtils.equals(threshold, Severity.INFO)) {
            thresholdValue = INFO_VALUE;
        } else if (StringUtils.equals(threshold, Severity.MINOR)) {
            thresholdValue = MINOR_VALUE;
        } else if (StringUtils.equals(threshold, Severity.MAJOR)) {
            thresholdValue = MAJOR_VALUE;
        } else if (StringUtils.equals(threshold, Severity.CRITICAL)) {
            thresholdValue = CRITICAL_VALUE;
        } else if (StringUtils.equals(threshold, Severity.BLOCKER)) {
            thresholdValue = BLOCKER_VALUE;
        } else {
            thresholdValue = UNKNOWN_VALUE;
        }

        return thresholdValue;
    }

    public static String valueToThreshold(int value) {
        String threshold = UNKNOWN;

        switch (value) {
        case INFO_VALUE:
            threshold = Severity.INFO;
            break;
        case MINOR_VALUE:
            threshold = Severity.MINOR;
            break;
        case MAJOR_VALUE:
            threshold = Severity.MAJOR;
            break;
        case CRITICAL_VALUE:
            threshold = Severity.CRITICAL;
            break;
        case BLOCKER_VALUE:
            threshold = Severity.BLOCKER;
            break;

        default:
            break;
        }

        return threshold;
    }

    public static int maxLevel(ReviewInput reviewInput) {
        int lvl = 0;

        for (Iterator<List<ReviewFileComment>> i = reviewInput.getComments().values().iterator(); i.hasNext();) {
            List<ReviewFileComment> lrfc = i.next();
            for (ReviewFileComment review : lrfc) {
                if (StringUtils.contains(review.getMessage(), Severity.INFO)) {
                    lvl = Math.max(lvl, thresholdToValue(Severity.INFO));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(LOG_MESSAGE, Severity.INFO, lvl);
                    }
                } else if (StringUtils.contains(review.getMessage(), Severity.MINOR)) {
                    lvl = Math.max(lvl, thresholdToValue(Severity.MINOR));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(LOG_MESSAGE, Severity.MINOR, lvl);
                    }
                } else if (StringUtils.contains(review.getMessage(), Severity.MAJOR)) {
                    lvl = Math.max(lvl, thresholdToValue(Severity.MAJOR));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(LOG_MESSAGE, Severity.MAJOR, lvl);
                    }
                } else if (StringUtils.contains(review.getMessage(), Severity.CRITICAL)) {
                    lvl = Math.max(lvl, thresholdToValue(Severity.CRITICAL));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(LOG_MESSAGE, Severity.CRITICAL, lvl);
                    }
                } else if (StringUtils.contains(review.getMessage(), Severity.BLOCKER)) {
                    lvl = Math.max(lvl, thresholdToValue(Severity.BLOCKER));
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(LOG_MESSAGE, Severity.BLOCKER, lvl);
                    }
                }
            }
        }

        return lvl;
    }

    private static String prefixKey(String prefix, String key) {
        return prefix + "." + key;
    }

    public static String issueMessage(String originalMessage, Settings settings, Issue issue) {
        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put(prefixKey(ISSUE_PREFIX, ISSUE_IS_NEW), issue.isNew());
        valueMap.put(prefixKey(ISSUE_PREFIX, ISSUE_RULE_KEY), issue.ruleKey());
        valueMap.put(prefixKey(ISSUE_PREFIX, ISSUE_SEVERITY), issue.severity());
        valueMap.put(prefixKey(ISSUE_PREFIX, ISSUE_MESSAGE), issue.message());
        return substituteProperties(originalMessage, settings, valueMap);
    }

	public static String substituteProperties(String originalMessage, Settings settings) {
        return substituteProperties(originalMessage, settings, Collections.<String, Object> emptyMap());
	}

    private static String substituteProperties(String originalMessage, Settings settings, Map<String, Object> additionalProperties) {
        if (additionalProperties.isEmpty()) {
            return StrSubstitutor.replace(originalMessage, settings.getProperties());
        }
        additionalProperties.putAll(settings.getProperties());
        return StrSubstitutor.replace(originalMessage, additionalProperties);
    }
}
