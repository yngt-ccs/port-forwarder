# なにこれ？
Java で動くSSH port fowrarder です。

## ビルド
```
mvnw clean package
```

## 準備
SSHの接続先単位で、こんな感じのJSON設定ファイルを準備します。
```
{
	"remoteHost": "[接続先のホスト名]",
	"remotePort": "22",
	"userName": "[ユーザー名]",
	"identityFile": "[秘密鍵]",
	"knownHostsFile": "[ホスト公開鍵(必要に応じて)]",
	"strictHostKeyChecking": [true(ホスト認証する時)/false(しない時)の二択],
	"proxyType": "[http/socks4/socks5 の3択]",
	"proxyHost": "[プロキシ ホスト]",
	"proxyPort": 3128,
	"localForwards": [
		{
			"localListenPort": [ローカルの待機ポート],
			"forwardToHost": "[転送先ホスト]",
			"forwardToPort": [転送先ポート]
		} ,
		...(複数定義可)
	]
}
```
あわせて、SSH認証に使う秘密鍵をファイルに準備しておきます。

## 使い方
ビルドしたJARと同じ階層に「conf」ディレクトリを準備して、接続設定JSON、秘密鍵、ホスト公開鍵を全て保存して下さい。

（こんな感じ）
- Port-Forwarder-0.0.1-SNAPSHOT.jar
- conf/
  - tunnel.json
  - id_rsa
  - known_hosts

コマンド プロンプト等のカレント ディレクトリをjar格納先に合わせ、java -jar 等で実行します。
```
C:\Users\yngt\git\port-forwarder>java -jar Port-Forwarder-0.0.1-SNAPSHOT.jar
```

## その他
### 複数同時に接続したい
- jsonファイルを conf/ の下に複数作成して下さい。

### conf ディレクトリが見つからないと出る
- java コマンド実行時のカレント ディレクトリを見直して下さい。（カレント直下のconf/を見に行きます）
- Eclipse で動かす場合、プロジェクト ディレクトリの直下にある conf を見に行くようです。

### 鍵ファイルの指定方法
- JSONで指定する鍵ファイルは、conf ディレクトリ内のファイル名と、conf ディレクトリ外のパスのいずれかを指定できます。
