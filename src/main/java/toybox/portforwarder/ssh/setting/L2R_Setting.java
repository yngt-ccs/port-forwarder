package toybox.portforwarder.ssh.setting;

import lombok.Data;

/**
 * ポート転送設定 (Local to Remote).
 */
@Data
public class L2R_Setting {

	/** ローカル 待機ポート. */
	private int localListenPort;

	/** 転送先ホスト. */
	private String forwardToHost;

	/** 転送先ポート */
	private int forwardToPort;

}
