package fr.techad.sonar.gerrit.network.ssh;

import fi.jpalomaki.ssh.Result;
import fi.jpalomaki.ssh.SshClient;
import fi.jpalomaki.ssh.UserAtHost;
import fi.jpalomaki.ssh.jsch.JschSshClient;
import fi.jpalomaki.ssh.jsch.JschSshClient.Options;
import fr.techad.sonar.GerritConfiguration;
import fr.techad.sonar.gerrit.GerritConnector;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

public class GerritSshConnector implements GerritConnector {
    private static final Logger LOG = Loggers.get(GerritSshConnector.class);
    private static final String CMD_LIST_FILES = "gerrit query --format=JSON --files --current-patch-set status:open change:%s limit:1";
    private static final String CMD_SET_REVIEW = "gerrit review %s -j";
    private static final String SSH_KNWON_HOSTS = ".ssh/known_hosts";
    private static final String SSH_STRICT_NO = "StrictHostKeyChecking=no";

    private final GerritConfiguration gerritConfiguration;
    private final UserAtHost userAtHost;

    public GerritSshConnector(GerritConfiguration gerritConfiguration) {
        LOG.debug("[GERRIT PLUGIN] Instanciating GerritSshConnector");
        this.gerritConfiguration = gerritConfiguration;
        userAtHost = new UserAtHost(gerritConfiguration.getUsername(), gerritConfiguration.getHost(),
            gerritConfiguration.getPort());
    }

    @NotNull
    @Override
    public String listFiles() throws IOException {
        SshClient sshClient = getSshClient();

        LOG.debug("[GERRIT PLUGIN] Execute command SSH {}",
            String.format(CMD_LIST_FILES, gerritConfiguration.getChangeId()));

        Result cmdResult = sshClient.executeCommand(String.format(CMD_LIST_FILES, gerritConfiguration.getChangeId()),
            userAtHost);

        return cmdResult.stdoutAsText("UTF-8");
    }

    @NotNull
    @Override
    public String setReview(String reviewInputAsJson) throws IOException {
        LOG.info("[GERRIT PLUGIN] Setting review {}", reviewInputAsJson);

        ByteBuffer stdin = ByteBuffer.wrap(reviewInputAsJson.getBytes("UTF-8"));
        SshClient sshClient = getSshClient();
        LOG.debug("[GERRIT PLUGIN] Execute command SSH {}",
            String.format(CMD_SET_REVIEW, gerritConfiguration.getRevisionId()));

        Result cmdResult = sshClient.executeCommand(String.format(CMD_SET_REVIEW, gerritConfiguration.getRevisionId()),
            stdin, userAtHost);

        return cmdResult.stdoutAsText();
    }

    private SshClient getSshClient() {
        SshClient sc = null;

        if (gerritConfiguration.shouldStrictlyCheckHostKey()) {
            LOG.debug("[GERRIT PLUGIN] SSH will check host key.");
            sc = new JschSshClient(gerritConfiguration.getSshKeyPath(), gerritConfiguration.getPassword());
        } else {
            LOG.debug("[GERRIT PLUGIN] SSH will not check host key.");
            String userKnownHosts = System.getProperty("user.home") + File.separator + SSH_KNWON_HOSTS;
            Boolean knownHostsExists = Files.exists(Paths.get(userKnownHosts), LinkOption.NOFOLLOW_LINKS);

            if (!knownHostsExists) {
                LOG.debug("[GERRIT PLUGIN] {} does not exist. Creating.", userKnownHosts);
                // known_hosts DOES NOT exists => create it
                try {
                    Files.createFile(Paths.get(userKnownHosts));
                } catch (IOException e) {
                    LOG.warn("[GERRIT PLUGIN] Could not create known_hosts", e);
                }
                LOG.debug("[GERRIT PLUGIN] {} created.", userKnownHosts);
            }

            sc = new JschSshClient(gerritConfiguration.getSshKeyPath(), gerritConfiguration.getPassword(),
                userKnownHosts, new Options("5s", "0s", "1M", "1M", SSH_STRICT_NO, false));
        }

        return sc;
    }
}
