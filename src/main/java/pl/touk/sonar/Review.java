package pl.touk.sonar;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.resources.Project;

public class Review {
    private final static Logger LOG = LoggerFactory.getLogger(Review.class);
    private String gerritHost;
    private Integer gerritPort;
    private String gerritUsername;
    private String gerritPassword;
    private String gerritProjectName;
    private String gerritChangeId;
    private String gerritRevisionId;
    private Project project;
    private DecoratorContext context;

    public Review(@NotNull Project project, @NotNull DecoratorContext context) {
        this.project = project;
        this.context = context;
    }

    public void validateGerritSettings() throws GerritPluginException {
        LOG.info(dumpGerritSettings());
        if (StringUtils.isBlank(gerritHost) ||
                gerritPort == null ||
                StringUtils.isBlank(gerritUsername) ||
                StringUtils.isBlank(gerritPassword) ||
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
                ", gerritPassword='" + (StringUtils.isBlank(gerritPassword) ? "blank" : "not blank") + '\'' +
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
    public Integer getGerritPort() {
        return gerritPort;
    }

    public void setGerritPort(@Nullable Integer gerritPort) {
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
    public String getGerritPassword() {
        return gerritPassword;
    }

    public void setGerritPassword(String gerritPassword) {
        this.gerritPassword = gerritPassword;
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

    @NotNull
    public DecoratorContext getContext() {
        return context;
    }
}
