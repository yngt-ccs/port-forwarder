package toybox.portforwarder;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import toybox.portforwarder.ssh.ConfReader;
import toybox.portforwarder.ssh.SSHSession;
import toybox.portforwarder.ssh.setting.ConnectionSetting;

public class PortForwarderApplication {

	protected static final Logger logger = LoggerFactory.getLogger(PortForwarderApplication.class);

	private static Map<String, Thread> threads = new HashMap<>();

	public static void main(String[] args) {
		start();
	}

	public static void start() {
		try {
			Map<String, ConnectionSetting> settings = ConfReader.readSettings();

			for (String name : settings.keySet()) {
				SSHSession session = new SSHSession(settings.get(name));

				Thread thread = new Thread(session);
				thread.setName("ssh-" + name);

				threads.put(name, thread);
				thread.start();
			}

			logger.info("initialized.");

		} catch (Throwable t) {
			logger.error("initialization failed. ", t);
		}
	}

//	public static void destroy() {
//		threads.keySet().parallelStream().forEach(k -> threads.get(k).interrupt());
//	}
}
