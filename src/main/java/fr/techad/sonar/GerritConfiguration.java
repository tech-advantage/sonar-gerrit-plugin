package fr.techad.sonar;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GerritConfiguration {
    private boolean enabled;

    private static final Logger LOG = LoggerFactory.getLogger(GerritConfiguration.class);
    private static GerritConfiguration gerritConf = new GerritConfiguration();
    private static GerritServerConfiguration serverConf = gerritConf.new GerritServerConfiguration();
    private static GerritReviewConfiguration reviewConf = gerritConf.new GerritReviewConfiguration();

    private GerritConfiguration() {
    }

    public static GerritServerConfiguration serverConfiguration() {
        return serverConf;
    }

    public static GerritReviewConfiguration reviewConfiguration() {
        return reviewConf;
    }

    public static boolean isValid() {
        return serverConf.isValid() && reviewConf.isValid();
    }

    class GerritServerConfiguration {
        private boolean serverValid = true;

        private String scheme;
        private String host;
        private Integer httpPort;
        private String httpUsername;
        private String httpPassword;
        private String authScheme;
        private String basePath;
        private boolean anonymous;

        protected GerritServerConfiguration() {
        }

        public GerritServerConfiguration enable(boolean serverEnabled) {
            enabled = serverEnabled;
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public boolean isAnonymous() {
            return anonymous;
        }

        @NotNull
        public String getScheme() {
            return scheme;
        }

        public GerritServerConfiguration setScheme(@NotNull String scheme) {
            this.scheme = scheme;
            return this;
        }

        @NotNull
        public String getHost() {
            return host;
        }

        public GerritServerConfiguration setHost(@NotNull String host) {
            this.host = host;
            return this;
        }

        @NotNull
        public Integer getHttpPort() {
            return httpPort;
        }

        public GerritServerConfiguration setHttpPort(@NotNull Integer httpPort) {
            this.httpPort = httpPort;
            return this;
        }

        @Nullable
        public String getHttpUsername() {
            return httpUsername;
        }

        public GerritServerConfiguration setHttpUsername(@Nullable String httpUsername) {
            this.httpUsername = httpUsername;
            if (StringUtils.isBlank(httpUsername)) {
                anonymous = true;
            }
            return this;
        }

        @Nullable
        public String getHttpPassword() {
            return httpPassword;
        }

        public GerritServerConfiguration setHttpPassword(String httpPassword) {
            this.httpPassword = httpPassword;
            return this;
        }

        public String getHttpAuthScheme() {
            return authScheme;
        }

        public GerritServerConfiguration setHttpAuthScheme(String authScheme) {
            this.authScheme = authScheme;
            return this;
        }

        @Nullable
        public String getBasePath() {
            return basePath;
        }

        public GerritServerConfiguration setBasePath(@Nullable String basePath) {
            String newBasePath = basePath;

            if (StringUtils.isBlank(newBasePath)) {
                newBasePath = "/";
            }

            if (newBasePath.charAt(0) != '/') {
                newBasePath = "/" + newBasePath;
            }

            while (newBasePath.startsWith("/", 1) && !newBasePath.isEmpty()) {
                newBasePath = newBasePath.substring(1, newBasePath.length());
            }

            while (newBasePath.endsWith("/") && 1 < newBasePath.length()) {
                newBasePath = newBasePath.substring(0, newBasePath.length() - 1);
            }

            this.basePath = newBasePath;

            return this;
        }

        void assertGerritServerConfiguration() {
            if (StringUtils.isBlank(host) || null == httpPort) {
                serverValid = false;
                if (enabled || LOG.isDebugEnabled()) {
                    LOG.error("[GERRIT PLUGIN] ServerConfiguration is not valid : {}", this.toString());
                }
            } else {
                serverValid = true;
            }
        }

        public boolean isValid() {
            assertGerritServerConfiguration();
            return serverValid;

        }

        @Override
        public String toString() {
            return "GerritServerConfiguration [serverValid=" + serverValid + ", enabled=" + enabled + ", scheme="
                    + scheme + ", host=" + host + ", httpPort=" + httpPort + ", httpUsername=" + httpUsername
                    + ", httpPassword=" + httpPassword + ", authScheme=" + authScheme + ", basePath=" + basePath + "]";
        }

    }

    class GerritReviewConfiguration {
        private boolean reviewValid = true;
        private String label;
        private String message;
        private String threshold;

        private String projectName;
        private String branchName;
        private String changeId;
        private String revisionId;

        protected GerritReviewConfiguration() {
        }

        @NotNull
        public String getLabel() {
            return label;
        }

        public GerritReviewConfiguration setLabel(@NotNull String label) {
            this.label = label;
            return this;
        }

        public String getMessage() {
            return message;
        }

        public GerritReviewConfiguration setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getThreshold() {
            return threshold;
        }

        public GerritReviewConfiguration setThreshold(String threshold) {
            this.threshold = threshold;
            return this;
        }

        @NotNull
        public String getProjectName() {
            return projectName;
        }

        public GerritReviewConfiguration setProjectName(@NotNull String projectName) {
            this.projectName = projectName;
            return this;
        }

        @NotNull
        public String getBranchName() {
            return branchName;
        }

        public GerritReviewConfiguration setBranchName(@NotNull String branchName) {
            this.branchName = branchName;
            return this;
        }

        @NotNull
        public String getChangeId() {
            return changeId;
        }

        public GerritReviewConfiguration setChangeId(@NotNull String changeId) {
            this.changeId = changeId;
            return this;
        }

        @NotNull
        public String getRevisionId() {
            return revisionId;
        }

        public GerritReviewConfiguration setRevisionId(@NotNull String revisionId) {
            this.revisionId = revisionId;
            return this;
        }

        void assertGerritReviewConfiguration() {
            if (StringUtils.isBlank(label) || StringUtils.isBlank(projectName) || StringUtils.isBlank(branchName)
                    || StringUtils.isBlank(changeId) || StringUtils.isBlank(revisionId)) {
                reviewValid = false;
                if (enabled || LOG.isDebugEnabled()) {
                    LOG.error("[GERRIT PLUGIN] ReviewConfiguration is not valid : {}", this.toString());
                }
            } else {
                reviewValid = true;
            }
        }

        public boolean isValid() {
            assertGerritReviewConfiguration();
            return reviewValid;

        }

        @Override
        public String toString() {
            return "GerritReviewConfiguration [reviewValid=" + reviewValid + ", label=" + label + ", message="
                    + message + ", threshold=" + threshold + ", projectName=" + projectName + ", branchName="
                    + branchName + ", changeId=" + changeId + ", revisionId=" + revisionId + "]";
        }
    }
}
