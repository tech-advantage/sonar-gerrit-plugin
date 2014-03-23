package pl.touk.sonar;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GerritConfiguration {
    private final static Logger LOG = LoggerFactory.getLogger(GerritConfiguration.class);
    private String scheme;
    private String host;
    private Integer httpPort;
    private String httpUsername;
    private String httpPassword;
    private String projectName;
    private String changeId;
    private String revisionId;
    private boolean valid = true;

    public void assertGerritConfiguration() {
        if (!valid) {
            return;
        }
        LOG.info(dump());
        valid = !(StringUtils.isBlank(host) ||
                httpPort == null ||
                StringUtils.isBlank(httpUsername) ||
                StringUtils.isBlank(httpPassword) ||
                StringUtils.isBlank(projectName) ||
                StringUtils.isBlank(changeId) ||
                StringUtils.isBlank(revisionId)
                );
    }

    private String dump() {
        return "Gerrit configuration: {" +
                "scheme='" + scheme + '\'' +
                ", host='" + host + '\'' +
                ", httpPort='" + httpPort + '\'' +
                ", httpUsername='" + httpUsername + '\'' +
                ", httpPassword='" + (StringUtils.isBlank(httpPassword) ? "blank" : "not blank") + '\'' +
                ", projectName='" + projectName + '\'' +
                ", changeId='" + changeId + '\'' +
                ", revisionId='" + revisionId + '\'' +
                '}';
    }

    @Nullable
    public String getScheme() {
        return scheme;
    }

    public void setScheme(@Nullable String scheme) {
        this.scheme = scheme;
    }

    @Nullable
    public String getHost() {
        return host;
    }

    public void setHost(@Nullable String host) {
        this.host = host;
    }

    @Nullable
    public Integer getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(@Nullable Integer httpPort) {
        this.httpPort = httpPort;
    }

    @Nullable
    public String getHttpUsername() {
        return httpUsername;
    }

    public void setHttpUsername(@Nullable String httpUsername) {
        this.httpUsername = httpUsername;
    }

    @Nullable
    public String getHttpPassword() {
        return httpPassword;
    }

    public void setHttpPassword(String httpPassword) {
        this.httpPassword = httpPassword;
    }

    @Nullable
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(@Nullable String projectName) {
        this.projectName = projectName;
    }

    @Nullable
    public String getChangeId() {
        return changeId;
    }

    public void setChangeId(@Nullable String changeId) {
        this.changeId = changeId;
    }

    @Nullable
    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(@Nullable String revisionId) {
        this.revisionId = revisionId;
    }

    public boolean isValid() {
        return valid;
    }
}
