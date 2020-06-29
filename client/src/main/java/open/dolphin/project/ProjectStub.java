package open.dolphin.project;

import open.dolphin.client.ClientContext;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.infomodel.UserModel;
import open.dolphin.inspector.InspectorCategory;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * プロジェクト情報管理クラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class ProjectStub implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final Preferences prefs;
    // Preferences のノード名
    private final String NODE_NAME = "/open/dolphin/project/v1310";
    // デフォルトのプロジェクト名
    private final String DEFAULT_PROJECT_NAME = "ASPOpenDolphin";
    // User ID
    private final String DEFAULT_USER_ID = null;
    private final String DEFAULT_FACILITY_ID = IInfoModel.DEFAULT_FACILITY_OID;
    // Server
    private final String DEFAULT_HOST_ADDRESS = "localhost";
    //private final int RMI_PORT = 1099;
    //private final int RMI_PORT = 4447; // jboss as 7.1
    private final int RMI_PORT = 8080; // REST
    // Claim
    private final boolean DEFAULT_SEND_CLAIM = false;
    private final boolean DEFAULT_SEND_CLAIM_SAVE = true;
    private final boolean DEFAULT_SEND_CLAIM_TMP = false;
    private final boolean DEFAULT_SEND_CLAIM_MODIFY = false;
    private final boolean DEFAULT_SEND_DIAGNOSIS = true;
    // Update
    private final boolean DEFAULT_USE_PROXY = false;
    private final String DEFAULT_PROXY_HOST = null;
    private final int DEFAULT_PROXY_PORT = 8080;
    private final long DEFAULT_LAST_MODIFIED = 0L;
    private boolean valid;
    private DolphinPrincipal principal;
    private String providerURL;
    private UserModel userModel;

    /**
     * ProjectStub を生成する.
     */
    public ProjectStub() {
        prefs = Preferences.userRoot().node(NODE_NAME);
    }

    /**
     * Preferencesを返す.
     *
     * @return preferences
     */
    public Preferences getPreferences() {
        return prefs;
    }

    /**
     * 設定ファイルが有効かどうかを返す.
     *
     * @return 有効な時 true
     */
    public boolean isValid() {

        // UserTypeを判定する
        if (getUserType().equals(Project.UserType.UNKNOWN)) {
            return false;
        }

        // UserIdとFacilityIdを確認する
        if (getUserId() == null || getFacilityId() == null) {
            return false;
        }

        // ここまで来れば有効である
        valid = true;
        return valid;
    }

    public DolphinPrincipal getDolphinPrincipal() {
        return principal;
    }

    public void setDolphinPrincipal(DolphinPrincipal principal) {
        this.principal = principal;
    }

    /**
     * ProviderURLを返す.
     *
     * @return JNDI に使用する ProviderURL
     */
    public String getProviderURL() {
        if (providerURL == null) {
            String host = prefs.get(Project.HOST_ADDRESS, DEFAULT_HOST_ADDRESS);
            int port = prefs.getInt(Project.HOST_PORT, RMI_PORT);

            //providerURL = String.format("jnp://%s:%d", host, port); // jboss 5
            providerURL = String.format("remote://%s:%d", host, 4447); // jboss 7
        }

        return providerURL;
    }

    /**
     * JNDIのProviderURLを設定する.
     *
     * @param providerURL JNDIのProviderURL
     */
    public void setProviderURL(String providerURL) {
        this.providerURL = providerURL;
    }

    /**
     * プロジェクト名を返す.
     *
     * @return プロジェクト名 (Dolphin ASP, HOT, MAIKO, HANIWA ... etc)
     */
    public String getName() {
        return prefs.get(Project.PROJECT_NAME, DEFAULT_PROJECT_NAME);
    }

    /**
     * プロジェクト名を返す.
     *
     * @param projectName project name
     */
    public void setName(String projectName) {
        prefs.put(Project.PROJECT_NAME, projectName);
    }

    /**
     * ログインユーザ情報を返す.
     *
     * @return Dolphinサーバに登録されているユーザ情報
     */
    public UserModel getUserModel() {
        return userModel;
    }

    /**
     * ログインユーザ情報を設定する.
     *
     * @param userModel ログイン時にDolphinサーバから取得した情報
     */
    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }

    /**
     * ログイン画面用のUserIDを返す.
     *
     * @return ログイン画面に表示するUserId
     */
    public String getUserId() {
        return prefs.get(Project.USER_ID, DEFAULT_USER_ID);
    }

    /**
     * ログイン画面用のUserIDを設定する.
     *
     * @param val ログイン画面に表示するUserId
     */
    public void setUserId(String val) {
        prefs.put(Project.USER_ID, val);
    }

    /**
     * ログイン画面用のFacilityIDを返す.
     *
     * @return ログイン画面に表示するFacilityID
     */
    public String getFacilityId() {
        return prefs.get(Project.FACILITY_ID, DEFAULT_FACILITY_ID);
    }

    /**
     * ログイン画面用のFacilityIDを設定する.
     *
     * @param val ログイン画面に表示するFacilityID
     */
    public void setFacilityId(String val) {
        prefs.put(Project.FACILITY_ID, val);
    }

    /**
     * 保存したパスワードをデコードして返す
     *
     * @return decoded password
     */
    public String getUserPassword() {
        return decryptPassword(getUserId(), prefs.get(Project.USER_PASSWORD, ""));
    }

    /**
     * ログイン時のパスワードを暗号化保存
     *
     * @param val password to save
     */
    public void setUserPassword(String val) {
        prefs.put(Project.USER_PASSWORD, encryptPassword(getUserId(), val));
    }

    /**
     * ログイン時のパスワードを保存するかどうか
     *
     * @return save password or not
     */
    public boolean isSavePassword() {
        return prefs.getBoolean(Project.SAVE_PASSWORD, false);
    }

    /**
     * ログイン時のパスワードを保存するかどうかを設定
     *
     * @param b save password or not
     */
    public void setSavePassword(boolean b) {
        prefs.putBoolean(Project.SAVE_PASSWORD, b);
    }

    /**
     * ORCA バージョンを返す.
     *
     * @return ORCA バージョン
     */
    public String getOrcaVersion() {
        return prefs.get("orcaVersion", "40");
    }

    /**
     * ORCA バージョンを設定する.
     *
     * @param version ORCA バージョン
     */
    public void setOrcaVersion(String version) {
        prefs.put("orcaVersion", version);
    }

    /**
     * JMARICode を返す.
     *
     * @return JMARI Code
     */
    public String getJMARICode() {
        return prefs.get("jmariCode", "JPN000000000000");
    }

    /**
     * JMARICode を設定する.
     *
     * @param jamriCode JMARI code
     */
    public void setJMARICode(String jamriCode) {
        prefs.put("jmariCode", jamriCode);
    }

    //
    // UserType
    //
    public Project.UserType getUserType() {
        // Preference 情報がない場合は　UNKNOWN を返す
        // これは Project.isValid() で必ずテストされる
        // String userType = prefs.get(Project.USER_TYPE, Project.UserType.UNKNOWN.toString());
        String userType = prefs.get(Project.USER_TYPE, Project.UserType.FACILITY_USER.toString());
        return Project.UserType.valueOf(userType);
    }

    public void setUserType(Project.UserType userType) {
        prefs.put(Project.USER_TYPE, userType.toString());
    }

    //
    // サーバ情報
    //
    public String getHostAddress() {
        return prefs.get(Project.HOST_ADDRESS, DEFAULT_HOST_ADDRESS);
    }

    public void setHostAddress(String val) {
        prefs.put(Project.HOST_ADDRESS, val);
        // HOST_ADRESS をセットしたら，providerURL もリセットする
        providerURL = null;
    }

    public int getHostPort() {
        return prefs.getInt(Project.HOST_PORT, RMI_PORT);
    }

    public void setHostPort(int val) {
        prefs.putInt(Project.HOST_PORT, val);
    }

    public String getTopInspector() {
        return prefs.get("topInspector", InspectorCategory.メモ.name()); // メモ
    }

    public void setTopInspector(String topInspector) {
        prefs.put("topInspector", topInspector);
    }

    public String getSecondInspector() {
        return prefs.get("secondInspector", InspectorCategory.病名.name()); // 病名
    }

    public void setSecondInspector(String secondInspector) {
        prefs.put("secondInspector", secondInspector);
    }

    public String getThirdInspector() {
        return prefs.get("thirdInspector", InspectorCategory.カレンダー.name()); // カレンダー
    }

    public void setThirdInspector(String thirdInspector) {
        prefs.put("thirdInspector", thirdInspector);
    }

    public String getForthInspector() {
        return prefs.get("forthInspector", InspectorCategory.文書履歴.name()); // 文書履歴
    }

    public void setForthInspector(String forthInspector) {
        prefs.put("forthInspector", forthInspector);
    }

    public String getFifthInspector() {
        return prefs.get("fifthInspector", InspectorCategory.アレルギー.name()); // アレルギー
    }

    public void setFifthInspector(String fifthInspector) {
        prefs.put("fifthInspector", fifthInspector);
    }

    public boolean getLocateByPlatform() {
        return prefs.getBoolean(Project.LOCATION_BY_PLATFORM, false);
    }

    public void setLocateByPlatform(boolean b) {
        prefs.putBoolean(Project.LOCATION_BY_PLATFORM, b);
    }

    public String getPDFStore() {
        String defaultStore = ClientContext.getPDFDirectory();
        return prefs.get("pdfStore", defaultStore);
    }

    public void setPDFStore(String pdfStore) {
        prefs.put("pdfStore", pdfStore);
    }

    public int getFetchKarteCount() {
        return prefs.getInt(Project.DOC_HISTORY_FETCHCOUNT, 1);
    }

    public void setFetchKarteCount(int cnt) {
        prefs.putInt(Project.DOC_HISTORY_FETCHCOUNT, cnt);
    }

    public boolean getScrollKarteV() {
        return prefs.getBoolean(Project.KARTE_SCROLL_DIRECTION, true);
    }

    public void setScrollKarteV(boolean b) {
        prefs.putBoolean(Project.KARTE_SCROLL_DIRECTION, b);
    }

    public boolean getAscendingKarte() {
        return prefs.getBoolean(Project.DOC_HISTORY_ASCENDING, false);
    }

    public void setAscendingKarte(boolean b) {
        prefs.putBoolean(Project.DOC_HISTORY_ASCENDING, b);
    }

    public int getKarteExtractionPeriod() {
        return prefs.getInt(Project.DOC_HISTORY_PERIOD, -12);
    }

    public void setKarteExtractionPeriod(int period) {
        prefs.putInt(Project.DOC_HISTORY_PERIOD, period);
    }

    public boolean getShowModifiedKarte() {
        return prefs.getBoolean(Project.DOC_HISTORY_SHOWMODIFIED, false);
    }

    public void setShowModifiedKarte(boolean b) {
        prefs.putBoolean(Project.DOC_HISTORY_SHOWMODIFIED, b);
    }

    public boolean getAscendingDiagnosis() {
        return prefs.getBoolean(Project.DIAGNOSIS_ASCENDING, false);
    }

    public void setAscendingDiagnosis(boolean b) {
        prefs.putBoolean(Project.DIAGNOSIS_ASCENDING, b);
    }

    public int getDiagnosisExtractionPeriod() {
        return prefs.getInt(Project.DIAGNOSIS_PERIOD, 0);
    }

    public void setDiagnosisExtractionPeriod(int period) {
        prefs.putInt(Project.DIAGNOSIS_PERIOD, period);
    }

    public int getDiagnosisOutcomeOffset() { return prefs.getInt(Project.OFFSET_OUTCOME_DATE, -7); }

    public void setDiagnosisOutcomeOffset(int offset) { prefs.putInt(Project.OFFSET_OUTCOME_DATE, offset); }

    public boolean isAutoOutcomeInput() {
        return prefs.getBoolean("autoOutcomeInput", false);
    }

    public void setAutoOutcomeInput(boolean b) {
        prefs.putBoolean("autoOutcomeInput", b);
    }

    public boolean isReplaceStamp() {
        return prefs.getBoolean("replaceStamp", false);
    }

    public void setReplaceStamp(boolean b) {
        prefs.putBoolean("replaceStamp", b);
    }

    public boolean isStampSpace() {
        return prefs.getBoolean("stampSpace", true);
    }

    public void setStampSpace(boolean b) {
        prefs.putBoolean("stampSpace", b);
    }

    public boolean isLaboFold() {
        return prefs.getBoolean("laboFold", true);
    }

    public void setLaboFold(boolean b) {
        prefs.putBoolean("laboFold", b);
    }

    public String getDefaultZyozaiNum() {
        return prefs.get("defaultZyozaiNum", "3");
    }

    public void setDefaultZyozaiNum(String defaultZyozaiNum) {
        prefs.put("defaultZyozaiNum", defaultZyozaiNum);
    }

    public String getDefaultMizuyakuNum() {
        return prefs.get("defaultMizuyakuNum", "1");
    }

    public void setDefaultMizuyakuNum(String defaultMizuyakuNum) {
        prefs.put("defaultMizuyakuNum", defaultMizuyakuNum);
    }

    public String getDefaultSanyakuNum() {
        return prefs.get("defaultSanyakuNum", "1.0");
    }

    public void setDefaultSanyakuNum(String defaultSanyakuNum) {
        prefs.put("defaultSanyakuNum", defaultSanyakuNum);
    }

    public String getDefaultRpNum() {
        return prefs.get("defaultRpNum", "3");
    }

    public void setDefaultRpNum(String defaultRpNum) {
        prefs.put("defaultRpNum", defaultRpNum);
    }

    public int getLabotestExtractionPeriod() {
        return prefs.getInt(Project.LABOTEST_PERIOD, -6);
    }

    public void setLabotestExtractionPeriod(int period) {
        prefs.putInt(Project.LABOTEST_PERIOD, period);
    }

    public boolean getConfirmAtNew() {
        return prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, true);
    }

    public void setConfirmAtNew(boolean b) {
        prefs.putBoolean(Project.KARTE_SHOW_CONFIRM_AT_NEW, b);
    }

    public int getCreateKarteMode() {
        return prefs.getInt(Project.KARTE_CREATE_MODE, 0); // 0=emptyNew, 1=applyRp, 2=copyNew
    }

    public void setCreateKarteMode(int mode) {
        prefs.getInt(Project.KARTE_CREATE_MODE, mode);
    }

    public boolean getPlaceKarteMode() {
        return prefs.getBoolean(Project.KARTE_PLACE_MODE, true);
    }

    public void setPlaceKarteMode(boolean mode) {
        prefs.putBoolean(Project.KARTE_PLACE_MODE, mode);
    }

    public boolean getConfirmAtSave() {
        return prefs.getBoolean(Project.KARTE_SHOW_CONFIRM_AT_SAVE, true);
    }

    public void setConfirmAtSave(boolean b) {
        prefs.putBoolean(Project.KARTE_SHOW_CONFIRM_AT_SAVE, b);
    }

    public int getPrintKarteCount() {
        return prefs.getInt(Project.KARTE_PRINT_COUNT, 0);
    }

    public void setPrintKarteCount(int cnt) {
        prefs.putInt(Project.KARTE_PRINT_COUNT, cnt);
    }

    public int getSaveKarteMode() {
        return prefs.getInt(Project.KARTE_SAVE_ACTION, 0); // 0=save 1=saveTmp
    }

    public void setSaveKarteMode(int mode) {
        prefs.putInt(Project.KARTE_SAVE_ACTION, mode); // 0=save 1=saveTmp
    }

    //
    // CLAIM関連情報
    //

    /**
     * CLAIM 送信全体への設定を返す.
     * デフォルトが false になっているのは新規インストールの場合で ORCA 接続なしで
     * 使えるようにするため.
     *
     * @return 送信する時 true
     */
    public boolean getSendClaim() {
        return prefs.getBoolean(Project.SEND_CLAIM, DEFAULT_SEND_CLAIM);
    }

    public void setSendClaim(boolean b) {
        prefs.putBoolean(Project.SEND_CLAIM, b);
    }

    /**
     * 保存時に CLAIM 送信を行うかどうかを返す.
     *
     * @return 行う時 true
     */
    public boolean getSendClaimSave() {
        return prefs.getBoolean(Project.SEND_CLAIM_SAVE, DEFAULT_SEND_CLAIM_SAVE);
    }

    public void setSendClaimSave(boolean b) {
        prefs.putBoolean(Project.SEND_CLAIM_SAVE, b);
    }

    /**
     * 仮保存時に CLAIM 送信を行うかどうかを返す.
     *
     * @return 行う時 true
     */
    public boolean getSendClaimTmp() {
        return prefs.getBoolean(Project.SEND_CLAIM_TMP, DEFAULT_SEND_CLAIM_TMP);
    }

    public void setSendClaimTmp(boolean b) {
        prefs.putBoolean(Project.SEND_CLAIM_TMP, b);
    }

    /**
     * 修正時に CLAIM 送信を行うかどうかを返す.
     *
     * @return 行う時 true
     */
    public boolean getSendClaimModify() {
        return prefs.getBoolean(Project.SEND_CLAIM_MODIFY, DEFAULT_SEND_CLAIM_MODIFY);
    }

    public void setSendClaimModify(boolean b) {
        prefs.putBoolean(Project.SEND_CLAIM_MODIFY, b);
    }

    public String getDefaultKarteTitle() {
        return prefs.get("defaultKarteTitle", "経過記録");
    }

    public void setDefaultKarteTitle(String defaultKarteTitle) {
        prefs.put("defaultKarteTitle", defaultKarteTitle);
    }

    public boolean isUseTop15AsTitle() {
        return prefs.getBoolean("useTop15AsTitle", true);
    }

    public void setUseTop15AsTitle(boolean useTop15AsTitle) {
        prefs.putBoolean("useTop15AsTitle", useTop15AsTitle);
    }

    /**
     * 病名 CLAIM 送信を行うかどうかを返す.
     *
     * @return 行う時 true
     */
    public boolean getSendDiagnosis() {
        return prefs.getBoolean(Project.SEND_DIAGNOSIS, DEFAULT_SEND_DIAGNOSIS);
    }

    public void setSendDiagnosis(boolean b) {
        prefs.putBoolean(Project.SEND_DIAGNOSIS, b);
    }

    //
    // Software Update 関連
    //
    public boolean getUseProxy() {
        return prefs.getBoolean(Project.USE_PROXY, DEFAULT_USE_PROXY);
    }

    public void setUseProxy(boolean b) {
        prefs.putBoolean(Project.USE_PROXY, b);
    }

    public String getProxyHost() {
        return prefs.get(Project.PROXY_HOST, DEFAULT_PROXY_HOST);
    }

    public void setProxyHost(String val) {
        prefs.put(Project.PROXY_HOST, val);
    }

    public int getProxyPort() {
        return prefs.getInt(Project.PROXY_PORT, DEFAULT_PROXY_PORT);
    }

    public void setProxyPort(int val) {
        prefs.putInt(Project.PROXY_PORT, val);
    }

    public long getLastModify() {
        return prefs.getLong(Project.LAST_MODIFIED, DEFAULT_LAST_MODIFIED);
    }

    public void setLastModify(long val) {
        prefs.putLong(Project.LAST_MODIFIED, val);
    }

    public void exportSubtree(OutputStream os) {
        try {
            prefs.exportSubtree(os);
        } catch (IOException | BackingStoreException e) {
            e.printStackTrace(System.err);
        }
    }

    public void clear() {
        try {
            prefs.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * パスワードを暗号化した文字列を返す.
     *
     * @param key key
     * @param pass password
     * @return encrypted password
     */
    public String encryptPassword(String key, String pass) {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();

        try (OutputStream outputStream = MimeUtility.encode(bo, "base64")) {
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.ENCRYPT_MODE, spec);

            outputStream.write(cipher.doFinal(pass.getBytes()));

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
            | IOException | IllegalBlockSizeException | BadPaddingException | MessagingException ex) {
            System.out.println("ProjectStub.java:" + ex);
        }

        //System.out.println("input password: " + pass);
        //System.out.println("encrypted password: " + bo.toString());

        return bo.toString();
    }

    /**
     * 暗号化したパスワードをデコードして返す.
     *
     * @param key key
     * @param pass password
     * @return decorded password
     */
    public String decryptPassword(String key, String pass) {
        if (StringUtils.isEmpty(key)) { return ""; }

        try (InputStream inputStream = MimeUtility.decode(new ByteArrayInputStream(pass.getBytes()), "base64");
             ByteArrayOutputStream bo = new ByteArrayOutputStream()) {

            byte[] buf = new byte[1024];
            int len = inputStream.read(buf);
            while (len != -1) {
                bo.write(buf, 0, len);
                len = inputStream.read(buf);
            }

            SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "Blowfish");
            Cipher cipher = Cipher.getInstance("Blowfish");
            cipher.init(Cipher.DECRYPT_MODE, spec);

            String decoded = new String(cipher.doFinal(bo.toByteArray()));

            //System.out.println("ecrypted password: " + pass);
            //System.out.println("decoded password: " + decoded);

            return decoded;

        } catch (MessagingException | IOException | NoSuchAlgorithmException | NoSuchPaddingException
            | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            ex.printStackTrace(System.err);
        }
        return "";
    }
}
