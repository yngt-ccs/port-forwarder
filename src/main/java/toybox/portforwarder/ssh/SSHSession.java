package toybox.portforwarder.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import toybox.portforwarder.ssh.setting.ConnectionSetting;
import toybox.portforwarder.ssh.setting.LocalForwardSetting;

/**
 * SSHセッション.
 */
public class SSHSession implements Runnable {

	private Logger logger;

	/** SSJ 接続設定. */
	private ConnectionSetting setting;

	/** Keep-Alive 送信間隔. */
	private static final int KEEPALIVE_INTERVAL_MILSEC = 5000;
	/** 接続時タイムアウト時間. */
	private static final int CONNECTION_TIMEOUT_MILSEC = 5000;
	/** 接続監視間隔. */
	private static final int MONITOR_INTERVAL_MILSEC = 3000;
	/** 再接続待ち時間. */
	private static final int RETRY_WAIT_MILSEC = 10000;

	/** Jsch セッション. */
	private Session session;

	/**
	 * コンストラクタ.
	 *
	 * @param setting 接続設定.
	 */
	public SSHSession(ConnectionSetting setting) {
		this.setting = setting;
		logger = LoggerFactory.getLogger(SSHSession.class.getName() + "." + setting.getConnectionName());
	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				loop();
			}
		} catch (InterruptedException e) {
			// 割込発生時 (=アプリ終了)
		} catch (Throwable t) {
			logger.error("Caught unknown throwable.", t);
		} finally {
			if (isConnected()) {
				disconnect();
			}
			logger.info("see you.");
		}
	}

	/**
	 * 接続維持ループ.
	 *
	 * @throws InterruptedException 割り込み発生時
	 */
	private void loop() throws InterruptedException {
		try {
			if (!isConnected()) {
				openSession();
			}

			logger.trace("monitoring interval");
			Thread.sleep(MONITOR_INTERVAL_MILSEC);

		} catch (JSchException e) {
			logger.warn("Caught JSchException. (reason=" + e.getMessage() + ")");
			logger.debug("Caught JSchException. cause=", e);
			disconnect();

			// loop wait
			logger.info("Please wait for retry. (waittime=" + RETRY_WAIT_MILSEC + " millisec.)");
			Thread.sleep(RETRY_WAIT_MILSEC);
		}
	}

	private boolean isConnected() {
		return session != null && session.isConnected();
	}

	private void disconnect() {
		if (session != null) {
			session.disconnect();
		}
		logger.info("Session disconnected.");
	}

	private void openSession() throws JSchException {
		logger.info("Connecting SSH session...");

		// JSch config
		JSch jsch = new JSch();
		if (setting.isStrictHostKeyChecking()) {
			String knownHosts = ConfReader.getConfFilePathAsString(setting.getKnownHostsFile());
			jsch.setKnownHosts(knownHosts);
		} else {
			JSch.setConfig("StrictHostKeyChecking", "no");
			logger.warn("HostKey checking disabled.");
		}

		// authn
		String identityFile = ConfReader.getConfFilePathAsString(setting.getIdentityFile());
		jsch.addIdentity(identityFile);
		session = jsch.getSession(setting.getUserName(), setting.getRemoteHost(), setting.getRemotePort());

		// connect option
		session.setServerAliveInterval(KEEPALIVE_INTERVAL_MILSEC);
		session.setTimeout(CONNECTION_TIMEOUT_MILSEC);

		// exec
		session.connect();
		Channel channel = session.openChannel("shell");
		channel.connect();

		logger.info("SSH Session connected.");

		// Port forward (local forward)
		for (LocalForwardSetting localForward : setting.getLocalForwards()) {
			String toHost = localForward.getForwardToHost();
			int toPort = localForward.getForwardToPort();
			int assinged_port = session.setPortForwardingL(localForward.getLocalListenPort(), toHost, toPort);
			logger.info("L2R Fowrad added. (localhost:" + assinged_port + " -> " + toHost + ":" + toPort + ")");
		}

	}

}