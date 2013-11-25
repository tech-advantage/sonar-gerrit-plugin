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
    private Integer gerritHttpPort;
    private String gerritHttpUsername;
    private String gerritHttpPassword;
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
                gerritHttpPort == null ||
                StringUtils.isBlank(gerritHttpUsername) ||
                StringUtils.isBlank(gerritHttpPassword) ||
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
                ", gerritHttpPort='" + gerritHttpPort + '\'' +
                ", gerritHttpUsername='" + gerritHttpUsername + '\'' +
                ", gerritHttpPassword='" + (StringUtils.isBlank(gerritHttpPassword) ? "blank" : "not blank") + '\'' +
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
    public Integer getGerritHttpPort() {
        return gerritHttpPort;
    }

    public void setGerritHttpPort(@Nullable Integer gerritHttpPort) {
        this.gerritHttpPort = gerritHttpPort;
    }

    @Nullable
    public String getGerritHttpUsername() {
        return gerritHttpUsername;
    }

    public void setGerritHttpUsername(@Nullable String gerritHttpUsername) {
        this.gerritHttpUsername = gerritHttpUsername;
    }

    @Nullable
    public String getGerritHttpPassword() {
        return gerritHttpPassword;
    }

    public void setGerritHttpPassword(String gerritHttpPassword) {
        this.gerritHttpPassword = gerritHttpPassword;
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
