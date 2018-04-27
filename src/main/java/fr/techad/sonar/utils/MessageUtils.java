package fr.techad.sonar.utils;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.config.Settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageUtils {

    private static final String ISSUE_PREFIX = "issue";
    private static final String ISSUE_IS_NEW = "isNew";
    private static final String ISSUE_RULE_KEY = "ruleKey";
    private static final String ISSUE_SEVERITY = "severity";
    private static final String ISSUE_MESSAGE = "message";
    private static final String ISSUE_SEPARATOR = ".";

    private MessageUtils() {
        super();
    }

    /**
     * Create the issue message. The variables contained in the original message
     * are replaced by the settings values and the issue data.
     *
     * @param originalMessage the original message
     * @param settings        the settings
     * @param issue           the issue
     * @return a new message with the replaced variables by data.
     */
    public static String createIssueMessage(String originalMessage, Settings settings, PostJobIssue issue) {
        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put(prefixKey(ISSUE_PREFIX, ISSUE_IS_NEW), issue.isNew());
        valueMap.put(prefixKey(ISSUE_PREFIX, ISSUE_RULE_KEY), issue.ruleKey());
        valueMap.put(prefixKey(ISSUE_PREFIX, ISSUE_SEVERITY), issue.severity());
        valueMap.put(prefixKey(ISSUE_PREFIX, ISSUE_MESSAGE), issue.message());
        return substituteProperties(originalMessage, settings, valueMap);
    }

    /**
     * Create the message from originalMessage and Settings. The variables
     * contained in the originalMessage are replaced by the Settings value
     *
     * @param originalMessage the original message which contains variables
     * @param settings        the settings
     * @return a new string with substituted variables.
     */
    public static String createMessage(String originalMessage, Settings settings) {
        return substituteProperties(originalMessage, settings, Collections.<String, Object>emptyMap());
    }

    /**
     * Build a string based on an original string and the replacement by
     * settings and map values
     *
     * @param originalMessage      the original string
     * @param settings             the settings
     * @param additionalProperties the additional values
     * @return the built message
     */
    private static String substituteProperties(String originalMessage, Settings settings,
                                               Map<String, Object> additionalProperties) {
        if (additionalProperties.isEmpty()) {
            return StrSubstitutor.replace(originalMessage, settings.getProperties());
        }
        additionalProperties.putAll(settings.getProperties());
        return StrSubstitutor.replace(originalMessage, additionalProperties);
    }

    /**
     * Create the key
     *
     * @param prefix the prefix key
     * @param key    the key
     * @return the key
     */
    private static String prefixKey(String prefix, String key) {
        return new StringBuffer(prefix).append(ISSUE_SEPARATOR).append(key).toString();
    }

}
