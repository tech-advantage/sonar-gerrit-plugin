package pl.touk.sonar;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.resources.Project;

public class Review {
    private final static Logger LOG = LoggerFactory.getLogger(Review.class);
    private String gerritHost;
    private String gerritPort;
    private String gerritUsername;
    private String gerritProjectName;
    private String gerritChangeId;
    private String gerritRevisionId;
    private Project project;

    public Review(@NotNull Project project) {
        this.project = project;
    }

    public void validateGerritSettings() throws GerritPluginException {
        LOG.info(dumpGerritSettings());
        if (StringUtils.isBlank(gerritHost) ||
                StringUtils.isBlank(gerritPort) ||
                StringUtils.isBlank(gerritUsername) ||
                StringUtils.isBlank(gerritProjectName) ||
                StringUtils.isBlank(gerritChangeId) ||
                StringUtils.isBlank(gerritRevisionId)
                ) {
            throw new GerritPluginException("Gerrit settings are not all filled");
        }
    }

    private String dumpGerritSettings() {
        return "Gerrit settings: {" +
                "gerritHost='" + gerritHost + '\'' +
                ", gerritPort='" + gerritPort + '\'' +
                ", gerritUsername='" + gerritUsername + '\'' +
                ", gerritProjectName='" + gerritProjectName + '\'' +
                ", gerritChangeId='" + gerritChangeId + '\'' +
                ", gerritRevisionId='" + gerritRevisionId + '\'' +
                '}';
    }

    @Nullable
    public String getGerritHost() {
        return gerritHost;
    }

    public void setGerritHost(@Nullable String gerritHost) {
        this.gerritHost = gerritHost;
    }

    @Nullable
    public String getGerritPort() {
        return gerritPort;
    }

    public void setGerritPort(@Nullable String gerritPort) {
        this.gerritPort = gerritPort;
    }

    @Nullable
    public String getGerritUsername() {
        return gerritUsername;
    }

    public void setGerritUsername(@Nullable String gerritUsername) {
        this.gerritUsername = gerritUsername;
    }

    @Nullable
    public String getGerritProjectName() {
        return gerritProjectName;
    }

    public void setGerritProjectName(@Nullable String gerritProjectName) {
        this.gerritProjectName = gerritProjectName;
    }

    @Nullable
    public String getGerritChangeId() {
        return gerritChangeId;
    }

    public void setGerritChangeId(@Nullable String gerritChangeId) {
        this.gerritChangeId = gerritChangeId;
    }

    @Nullable
    public String getGerritRevisionId() {
        return gerritRevisionId;
    }

    public void setGerritRevisionId(@Nullable String gerritRevisionId) {
        this.gerritRevisionId = gerritRevisionId;
    }

    @NotNull
    public Project getProject() {
        return project;
    }
}
