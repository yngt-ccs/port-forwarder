package toybox.portforwarder.ssh.setting;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * 接続設定.
 */
@Data
public class ConnectionSetting {

	/** 接続名. */
	@JsonIgnore
	private String connectionName;

	/** SSH接続先 リモート ホスト. */
	private String remoteHost;
	/** SSH接続先 リモート ポート. */
	private Integer remotePort;
	/** SSH認証 ユーザー名. */
	private String userName;
	/** SSH認証 秘密鍵. */
	private String identityFile;

	/** ホスト公開鍵. */
	private String knownHostsFile;

	/** ホスト認証要否. */
	private boolean strictHostKeyChecking = true;

	/** ポート転送設定(Local forward). */
	private final List<LocalForwardSetting> localForwards = new ArrayList<>();
}
