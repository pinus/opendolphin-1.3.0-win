package open.dolphin.orca;

import open.dolphin.JsonConverter;
import open.dolphin.orca.orcaapi.OrcaApiUrl;
import org.jboss.logging.Logger;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * OrcaHostInfo. ORCA の接続情報を何らかの方法で取得するクラス.
 *
 * @author pns
 */
@Startup
@Singleton
public class OrcaHostInfo {
    private static final OrcaHostInfo ORCA_HOST_INFO = new OrcaHostInfo();
    private static final String ORCA_HOST_INFO_FILE = "orca.host.info";

    private Logger logger = Logger.getLogger(OrcaHostInfo.class);

    /**
     * 取得した ORCA 情報.
     */
    private HostData hostData;

    /**
     * OrcaHostInfo.
     * ORCA の接続情報を ORCA_HOST_INFO_FILE (JSON) から読み取ってインスタンスを作る.
     * ORCA への接続を試みて，接続が成功するまで待つ.
     */
    private OrcaHostInfo() {
        try {
            String jBossBaseDir = System.getProperty("jboss.server.base.dir");
            Path pref = Paths.get(jBossBaseDir + "/deployments/" + ORCA_HOST_INFO_FILE);
            String json = String.join("", Files.readAllLines(pref));
            hostData = JsonConverter.fromJson(json, HostData.class);
            logger.info("orca.host.info=" + json);

        } catch (IOException e) {
            // ファイルがない場合
            hostData = new HostData();
            logger.info("Set default OrcaHostInfo: " + hostData.getHost());
        }

        // ORCA が立ち上がるまで待つ
        int retry = 0;
        URI uri = getOrcaApiUri(OrcaApiUrl.PATIENTGETV2);
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        getUserId(),
                        getPassword().toCharArray());
            }
        });
        while (true) {
            try {
                URLConnection con = uri.toURL().openConnection();
                try (InputStream in = con.getInputStream()) {
                }
                logger.info("ORCA server responded.");
                break;

            } catch (IOException ex) {
                logger.info(ex.getMessage() + ", retrying: " + ++retry);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * OrcaHostInfo のインスタンス.
     *
     * @return OrcaHostInfo
     */
    public static OrcaHostInfo getInstance() {
        return ORCA_HOST_INFO;
    }

    /**
     * ORCA のアドレス. ポート番号は含まない.
     *
     * @return the host
     */
    public String getHost() {
        return hostData.getHost();
    }

    /**
     * ORCA 用の userId.
     *
     * @return the userId
     */
    public String getUserId() {
        return hostData.getUserId();
    }

    /**
     * ORCA 用の userId の password.
     *
     * @return the password
     */
    public String getPassword() {
        return hostData.getPassword();
    }

    /**
     * JMARI コード.
     *
     * @return the password
     */
    public String getJmariCode() {
        return hostData.getJmari();
    }

    /**
     * Orca Api の URL にホスト名などを加えて URI を作る.
     *
     * @param url OrcaApi URL
     * @return URI
     */
    public URI getOrcaApiUri(String url) {
        try {
            return new URI(String.format("http://%s:8000%s", getHost(), url));

        } catch (URISyntaxException ex) {
            ex.printStackTrace(System.err);

            return null;
        }
    }

    /**
     * PushAPI の URI を作る.
     *
     * @return URI
     */
    public URI getPushApiUri() {
        String url = String.format("ws://%s:9400/ws", getHost());
        return URI.create(url);
    }

    /**
     * OrcaDao でデータベースに接続するための URL を返す.
     *
     * @return JDBC URL
     */
    public String getJdbcUrl() {
        return String.format("jdbc:postgresql://%s:5432/orca", getHost());
    }

    /**
     * OrcaDao でデータベースに接続するための Properties を返す.
     *
     * @return JDBC Properties
     */
    public Properties getJdbcProperties() {
        Properties p = new Properties();
        p.setProperty("user", hostData.getJdbcUserId());
        p.setProperty("password", hostData.getJdbcPassword());
        return p;
    }

    /**
     * ORCA_HOST_INFO_FILE から読み取った情報を入れるクラス.
     */
    private static class HostData {
        /**
         * ORCA のアドレス. ポート番号は含まない.
         */
        private String host = "trial.orca.med.or.jp";
        /**
         * ORCA 用の userId.
         */
        private String userId = "trial";
        /**
         * ORCA 用の userId の password.
         */
        private String password = "";
        /**
         * JMARI コード.
         */
        private String jmari = "JPN000000000000";
        /**
         * JDBC 接続用 user name.
         */
        private String jdbcUserId = "doctor1";
        /**
         * JDBC 接続用パスワード.
         */
        private String jdbcPassword = "";

        public String getHost() {
            return host;
        }

        public String getUserId() {
            return userId;
        }

        public String getPassword() {
            return password;
        }

        public String getJmari() {
            return jmari;
        }

        public String getJdbcUserId() {
            return jdbcUserId;
        }

        public String getJdbcPassword() {
            return jdbcPassword;
        }
    }
}
