package toybox.portforwarder.ssh;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import toybox.portforwarder.ssh.setting.ConnectionSetting;

/**
 * SSHセッション管理.
 */
@Service
public class SSHSessionManager {

	protected static final Logger logger = LoggerFactory.getLogger(SSHSessionManager.class);

	private Map<String, Thread> threads = new HashMap<>();

	@PostConstruct
	public void init() {
		try {
			Map<String, ConnectionSetting> settings = ConfReader.readSettings();

			for (String name : settings.keySet()) {
				SSHSession session = new SSHSession(settings.get(name));

				Thread thread = new Thread(session);
				thread.setName("ssh-" + name);

				threads.put(name, thread);
				thread.start();

				logger.info("initialized.");
			}

		} catch (Throwable t) {
			logger.error("initialization failed. ", t);
		}
	}

	@PreDestroy
	public void destroy() {
		threads.keySet().parallelStream().forEach(k -> threads.get(k).interrupt());
	}
}
