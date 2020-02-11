package toybox.portforwarder.ssh;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import toybox.portforwarder.ssh.setting.ConnectionSetting;

public class ConfReader {

	protected static final Logger logger = LoggerFactory.getLogger(ConfReader.class);

	public static final String CONF_DIR_NAME = "conf";

	/**
	 * 設定読み込み.
	 * @return
	 */
	public static Map<String, ConnectionSetting> readSettings() {
		Map<String,ConnectionSetting> resultSettings = new LinkedHashMap<>();

		File confDir = getConfDirAsFile();
		List<File> confFiles = Arrays.asList(confDir.listFiles());
		List<File> jsonFiles = confFiles.parallelStream().filter(f -> f.getName().toLowerCase().endsWith(".json"))
				.collect(Collectors.toList());

		ObjectMapper mapper = new ObjectMapper();
		for (File jsonFile : jsonFiles) {
			try {
				// 接続名 (拡張子を抜いたファイル名)
				String connectionName = StringUtils.split(jsonFile.getName(), ".")[0];

				// 接続構成をJSONより取得
				ConnectionSetting setting = mapper.readValue(jsonFile, ConnectionSetting.class);
				setting.setConnectionName(connectionName);

				resultSettings.put(connectionName, setting);
			} catch (IOException e) {
				logger.debug("Caught IOException.", e);
				logger.info(jsonFile.toString() + " can't read. (reason: " + e.getMessage() + ")");
			}
		}

		return resultSettings;
	}

	/**
	 * ファイルパス 文字列取得.
	 *
	 * @param fileName ファイル名
	 * @return ファイルの絶対パス
	 */
	public static String getConfFilePathAsString(String fileName) {
		if (fileName.indexOf(File.separator) != -1) {
			// ファイル パスを指定された場合、そのまま返す
			return fileName;
		} else {
			// ファイル名のみ指定された場合、設定ディレクトリ上のファイル パスを返す
			return getConfDirAsFile().toString() + File.separator + fileName;
		}
	}

	/**
	 * @return 設定ディレクトリ
	 */
	public static File getConfDirAsFile() {
		String confDirPath = getCurrentDirPath().toString() + File.separator + CONF_DIR_NAME;
		File confDirFile = Paths.get(confDirPath).toFile();

		if (!confDirFile.exists() || !confDirFile.isDirectory()) {
			return null;
		}

		return confDirFile;
	}

	/**
	 * @return カレント ディレクトリ パス
	 */
	private static Path getCurrentDirPath() {
		 String path = new File(".").getAbsoluteFile().getParent();
		 return Paths.get(path);
	}
}
