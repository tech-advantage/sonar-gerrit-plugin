package fr.techad.sonar.gerrit;

import java.io.IOException;
import java.nio.ByteBuffer;

import fi.jpalomaki.ssh.Result;
import fi.jpalomaki.ssh.SshClient;
import fi.jpalomaki.ssh.UserAtHost;
import fi.jpalomaki.ssh.jsch.JschSshClient;

import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import fr.techad.sonar.GerritConfiguration;

public class GerritSshConnector implements GerritConnector {
	private static final Logger LOG = Loggers.get(GerritSshConnector.class);
	private static final String CMD_LIST_FILES = "gerrit query --format=JSON --files --current-patch-set status:open change:%s limit:1";
	private static final String CMD_SET_REVIEW = "gerrit review %s -j";
	private final GerritConfiguration gerritConfiguration;
	private final UserAtHost userAtHost;

	public GerritSshConnector(GerritConfiguration gerritConfiguration) {
		LOG.debug("[GERRIT PLUGIN] Instanciating GerritSshConnector");
		this.gerritConfiguration = gerritConfiguration;
		userAtHost = new UserAtHost(gerritConfiguration.getUsername(), gerritConfiguration.getHost(),
				gerritConfiguration.getPort());
	}

	@NotNull
	public String listFiles() throws IOException {
		SshClient sshClient = new JschSshClient(gerritConfiguration.getSshKeyPath(), gerritConfiguration.getPassword());

		LOG.debug("[GERRIT PLUGIN] Execute command SSH {}",
				String.format(CMD_LIST_FILES, gerritConfiguration.getChangeId()));

		Result cmdResult = sshClient.executeCommand(String.format(CMD_LIST_FILES, gerritConfiguration.getChangeId()),
				userAtHost);

		return cmdResult.stdoutAsText("UTF-8");
	}

	@NotNull
	public String setReview(String reviewInputAsJson) throws IOException {
		LOG.info("[GERRIT PLUGIN] Setting review {}", reviewInputAsJson);

		ByteBuffer stdin = ByteBuffer.wrap(reviewInputAsJson.getBytes("UTF-8"));
		SshClient sshClient = new JschSshClient(gerritConfiguration.getSshKeyPath(), gerritConfiguration.getPassword());

		LOG.debug("[GERRIT PLUGIN] Execute command SSH {}",
				String.format(CMD_SET_REVIEW, gerritConfiguration.getRevisionId()));

		Result cmdResult = sshClient.executeCommand(String.format(CMD_SET_REVIEW, gerritConfiguration.getRevisionId()),
				stdin, userAtHost);

		return cmdResult.stdoutAsText();
	}
}
