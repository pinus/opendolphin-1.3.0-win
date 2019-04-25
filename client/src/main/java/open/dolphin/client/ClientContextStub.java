package open.dolphin.client;

import open.dolphin.infomodel.DepartmentModel;
import open.dolphin.infomodel.DiagnosisCategoryModel;
import open.dolphin.infomodel.DiagnosisOutcomeModel;
import open.dolphin.infomodel.LicenseModel;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dolphin Client のコンテキストクラス.
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public final class ClientContextStub {

    private final String RESOURCE_LOCATION = "/";
    private final String TEMPLATE_LOCATION = "/templates/";
    private final String IMAGE_LOCATION = "/images/";
    private final String SCHEMA_LOCATION = "/schema/";
    private final String RESOURCE = "Dolphin_ja";
    private final ResourceBundle resBundle;
    private final ClassLoader pluginClassLoader;
    private String documentFolder;
    private Logger bootLogger;
    private Logger part11Logger;
    private Logger delegaterLogger;
    private Logger pvtLogger;
    private Logger laboTestLogger;
    private Logger claimLogger;
    private Logger mmlLogger;
    private HashMap<String, Color> eventColorTable;
    private boolean isMac, isWin, isLinux;

    /**
     * ClientContextStub オブジェクトを生成する.
     */
    public ClientContextStub() {

        // ResourceBundle を得る
        resBundle = ResourceBundle.getBundle(RESOURCE);

        // Logger を生成する
        generateLoggers();

        // Log4J のコンフィグレーションを行う
        PropertyConfigurator.configure(getResource("log4j.prop"));

        // 基本情報を出力する
        logStartupInformation();

        // Plugin Class Loader を生成する
        pluginClassLoader = Thread.currentThread().getContextClassLoader();

        // デフォルトの UI フォントを変更する
        setUIFonts();

        // OS情報
        String osname = System.getProperty("os.name").toLowerCase();
        isMac = osname.startsWith("mac");
        isWin = osname.startsWith("windows");
        isLinux = osname.startsWith("linux");

        // Document フォルダの場所
        documentFolder = isWin() ? "Z:\\" : "/Volumes/documents/";
    }

    private void generateLoggers() {
        bootLogger = Logger.getLogger("boot.logger");
        part11Logger = Logger.getLogger("part11.logger");
        delegaterLogger = Logger.getLogger("delegater.logger");
        pvtLogger = Logger.getLogger("pvt.logger");
        laboTestLogger = Logger.getLogger("laboTest.logger");
        claimLogger = Logger.getLogger("claim.logger");
        mmlLogger = Logger.getLogger("mml.logger");
    }

    private void logStartupInformation() {
        bootLogger.info("起動時刻 = " + DateFormat.getDateTimeInstance().format(new Date()));
        bootLogger.info("os.name = " + System.getProperty("os.name"));
        bootLogger.info("java.version = " + System.getProperty("java.version"));
        bootLogger.info("dolphin.version = " + getString("version"));
        bootLogger.info("base.directory = " + getString("base.dir"));
        bootLogger.info("lib.directory = " + getString("lib.dir"));
        bootLogger.info("plugins.directory = " + getString("plugins.dir"));
        bootLogger.info("log.directory = " + getString("log.dir"));
        bootLogger.info("setting.directory = " + getString("setting.dir"));
        bootLogger.info("security.directory = " + getString("security.dir"));
        bootLogger.info("schema.directory = " + getString("schema.dir"));
        bootLogger.info("log.config.file = " + getString("log.config.file"));
        bootLogger.info("veleocity.log.file = " + getString("application.velocity.log.file"));
        bootLogger.info("login.config.file = " + getString("application.security.login.config"));
        bootLogger.info("ssl.trsutStore = " + getString("application.security.ssl.trustStore"));
    }

    public String getDocumentDirectory() { return documentFolder; }

    public ClassLoader getPluginClassLoader() {
        return pluginClassLoader;
    }

    public Logger getBootLogger() {
        return bootLogger;
    }

    public Logger getPart11Logger() {
        return part11Logger;
    }

    public Logger getClaimLogger() {
        return claimLogger;
    }

    public Logger getMmlLogger() {
        return mmlLogger;
    }

    public Logger getPvtLogger() {
        return pvtLogger;
    }

    public Logger getDelegaterLogger() {
        return delegaterLogger;
    }

    public Logger getLaboTestLogger() {
        return laboTestLogger;
    }

    public boolean isMac() { return isMac; }

    public boolean isWin() {
        return isWin;
    }

    public boolean isLinux() {
        return isLinux;
    }

    public String getLocation(String dir) {

        String ret = null;
        StringBuilder sb = new StringBuilder();

        // sb.append(System.getProperty(getString("base.dir")));
        // AppBundler が base.dir を正しく返さないのの workaround
        try {
            Path path = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (path.getFileName().toString().contains(".jar")) {
                // jar ファイルや .app から立ち上げている場合
                sb.append(path.getParent().toString());

            } else {
                // netbeans から立ち上げている場合は従来通り
                sb.append(System.getProperty(getString("base.dir")));
            }

        } catch (URISyntaxException ex) {
            ex.printStackTrace(System.err);
        }

        switch (dir) {
            case "base":
                ret = sb.toString();
                break;
            case "lib":
                sb.append(File.separator);
                if (isMac()) {
                    sb.append(getString("lib.mac.dir"));
                } else {
                    sb.append(getString("lib.dir"));
                }
                ret = sb.toString();
                break;
            case "dolphin.jar":
                if (isMac()) {
                    sb.append(File.separator);
                    sb.append(getString("dolphin.jar.mac.dir"));
                }
                ret = sb.toString();
                break;
            case "security":
                sb.append(File.separator);
                sb.append(getString("security.dir"));
                ret = sb.toString();
                break;
            case "log":
                sb.append(File.separator);
                sb.append(getString("log.dir"));
                ret = sb.toString();
                break;
            case "setting":
                sb.append(File.separator);
                sb.append(getString("setting.dir"));
                ret = sb.toString();
                break;
            case "schema":
                sb.append(File.separator);
                sb.append(getString("schema.dir"));
                ret = sb.toString();
                break;
            case "plugins":
                sb.append(File.separator);
                sb.append(getString("plugins.dir"));
                ret = sb.toString();
                break;
            case "pdf":
                sb.append(File.separator);
                sb.append(getString("pdf.dir"));
                ret = sb.toString();
                break;
            default:
                break;
        }

        return ret;
    }

    public String getBaseDirectory() {
        return getLocation("base");
    }

    public String getPluginsDirectory() {
        return getLocation("plugins");
    }

    public String getSettingDirectory() {
        return getLocation("setting");
    }

    public String getSecurityDirectory() {
        return getLocation("security");
    }

    public String getLogDirectory() {
        return getLocation("log");
    }

    public String getLibDirectory() {
        return getLocation("lib");
    }

    public String getPDFDirectory() {
        return getLocation("pdf");
    }

    public String getDolphinJarDirectory() {
        return getLocation("dolphin.jar");
    }

    public String getVersion() {
        return getString("version");
    }

    public String getUpdateURL() {

        if (isMac()) {
            return getString("updater.url.mac");
        } else if (isWin()) {
            return getString("updater.url.win");
        } else if (isLinux()) {
            return getString("updater.url.linux");
        } else {
            return getString("updater.url.linux");
        }
    }

    public String getFrameTitle(String title) {
        try {
            String resTitle = getString(title);
            if (resTitle != null) {
                title = resTitle;
            }
        } catch (Exception e) {
            // ここの exception は無害
            // System.out.println("ClientContextStub.java: " + e);
        }

        return String.format("%s-%s-%s", title, getString("application.title"), getString("version"));
    }

    public URL getResource(String name) {
        if (!name.startsWith("/")) {
            name = RESOURCE_LOCATION + name;
        }
        return this.getClass().getResource(name);
    }

    public URL getImageResource(String name) {
        if (!name.startsWith("/")) {
            name = IMAGE_LOCATION + name;
        }
        return this.getClass().getResource(name);
    }

    public InputStream getResourceAsStream(String name) {
        if (!name.startsWith("/")) {
            name = RESOURCE_LOCATION + name;
        }
        return this.getClass().getResourceAsStream(name);
    }

    public InputStream getTemplateAsStream(String name) {
        if (!name.startsWith("/")) {
            name = TEMPLATE_LOCATION + name;
        }
        return this.getClass().getResourceAsStream(name);
    }

    public ImageIcon getImageIcon(String name) {
        return new ImageIcon(getImageResource(name));
    }

    public ImageIcon getSchemaIcon(String name) {
        if (!name.startsWith("/")) {
            name = SCHEMA_LOCATION + name;
        }
        return new ImageIcon(this.getClass().getResource(name));
    }

    public LicenseModel[] getLicenseModel() {
        String[] desc = getStringArray("licenseDesc");
        String[] code = getStringArray("license");
        String codeSys = getString("licenseCodeSys");
        LicenseModel[] ret = new LicenseModel[desc.length];
        LicenseModel model;
        for (int i = 0; i < desc.length; i++) {
            model = new LicenseModel();
            model.setLicense(code[i]);
            model.setLicenseDesc(desc[i]);
            model.setLicenseCodeSys(codeSys);
            ret[i] = model;
        }
        return ret;
    }

    public DepartmentModel[] getDepartmentModel() {
        String[] desc = getStringArray("departmentDesc");
        String[] code = getStringArray("department");
        String codeSys = getString("departmentCodeSys");
        DepartmentModel[] ret = new DepartmentModel[desc.length];
        DepartmentModel model;
        for (int i = 0; i < desc.length; i++) {
            model = new DepartmentModel();
            model.setDepartment(code[i]);
            model.setDepartmentDesc(desc[i]);
            model.setDepartmentCodeSys(codeSys);
            ret[i] = model;
        }
        return ret;
    }

    public DiagnosisOutcomeModel[] getDiagnosisOutcomeModel() {
        String[] desc = getStringArray("diagnosis.outcomeDesc");
        String[] code = getStringArray("diagnosis.outcome");
        String codeSys = getString("diagnosis.outcomeCodeSys");
        DiagnosisOutcomeModel[] ret = new DiagnosisOutcomeModel[desc.length];
        DiagnosisOutcomeModel model;
        for (int i = 0; i < desc.length; i++) {
            model = new DiagnosisOutcomeModel();
            model.setOutcome(code[i]);
            model.setOutcomeDesc(desc[i]);
            model.setOutcomeCodeSys(codeSys);
            ret[i] = model;
        }
        return ret;
    }

    public DiagnosisCategoryModel[] getDiagnosisCategoryModel() {
        String[] desc = getStringArray("diagnosis.outcomeDesc");
        String[] code = getStringArray("diagnosis.outcome");
        String[] codeSys = getStringArray("diagnosis.outcomeCodeSys");
        DiagnosisCategoryModel[] ret = new DiagnosisCategoryModel[desc.length];
        DiagnosisCategoryModel model;
        for (int i = 0; i < desc.length; i++) {
            model = new DiagnosisCategoryModel();
            model.setDiagnosisCategory(code[i]);
            model.setDiagnosisCategoryDesc(desc[i]);
            model.setDiagnosisCategoryCodeSys(codeSys[i]);
            ret[i] = model;
        }
        return ret;
    }

    public NameValuePair[] getNameValuePair(String key) {
        NameValuePair[] ret;
        String[] code = getStringArray(key + ".value");
        String[] name = getStringArray(key + ".name");
        int len = code.length;
        ret = new NameValuePair[len];

        for (int i = 0; i < len; i++) {
            ret[i] = new NameValuePair(name[i], code[i]);
        }
        return ret;
    }

    public HashMap<String, Color> getEventColorTable() {
        if (eventColorTable == null) {
            setupEventColorTable();
        }
        return eventColorTable;
    }

    private void setupEventColorTable() {
        // イベントカラーを定義する
        eventColorTable = new HashMap<>();
        eventColorTable.put("TODAY", getColor("color.TODAY_BACK"));
        eventColorTable.put("BIRTHDAY", getColor("color.BIRTHDAY_BACK"));
        eventColorTable.put("PVT", getColor("color.PVT"));
        eventColorTable.put("DOC_HISTORY", getColor("color.PVT"));
    }

    public String getString(String key) {
        return resBundle.getString(key);
    }

    public String[] getStringArray(String key) {
        String line = getString(key);
        return line.split(",");
    }

    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public int[] getIntArray(String key) {
        String[] obj = getStringArray(key);
        int[] ret = new int[obj.length];
        for (int i = 0; i < obj.length; i++) {
            ret[i] = Integer.parseInt(obj[i]);
        }
        return ret;
    }

    public long getLong(String key) {
        return Long.parseLong(getString(key));
    }

    public long[] getLongArray(String key) {
        String[] obj = getStringArray(key);
        long[] ret = new long[obj.length];
        for (int i = 0; i < obj.length; i++) {
            ret[i] = Long.parseLong(obj[i]);
        }
        return ret;
    }

    public float getFloat(String key) {
        return Float.parseFloat(getString(key));
    }

    public float[] getFloatArray(String key) {
        String[] obj = getStringArray(key);
        float[] ret = new float[obj.length];
        for (int i = 0; i < obj.length; i++) {
            ret[i] = Float.parseFloat(obj[i]);
        }
        return ret;
    }

    public double getDouble(String key) {
        return Double.parseDouble(getString(key));
    }

    public double[] getDoubleArray(String key) {
        String[] obj = getStringArray(key);
        double[] ret = new double[obj.length];
        for (int i = 0; i < obj.length; i++) {
            ret[i] = Double.parseDouble(obj[i]);
        }
        return ret;
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    public boolean[] getBooleanArray(String key) {
        String[] obj = getStringArray(key);
        boolean[] ret = new boolean[obj.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Boolean.parseBoolean(obj[i]);
        }
        return ret;
    }

    public Point lgetPoint(String name) {
        int[] data = getIntArray(name);
        return new Point(data[0], data[1]);
    }

    public Dimension getDimension(String name) {
        int[] data = getIntArray(name);
        return new Dimension(data[0], data[1]);
    }

    public Insets getInsets(String name) {
        int[] data = getIntArray(name);
        return new Insets(data[0], data[1], data[2], data[3]);
    }

    public Color getColor(String key) {
        int[] data = getIntArray(key);
        return new Color(data[0], data[1], data[2]);
    }

    public Color[] getColorArray(String key) {
        int[] data = getIntArray(key);
        int cnt = data.length / 3;
        Color[] ret = new Color[cnt];
        for (int i = 0; i < cnt; i++) {
            int bias = i * 3;
            ret[i] = new Color(data[bias], data[bias + 1], data[bias + 2]);
        }
        return ret;
    }

    public Class[] getClassArray(String name) {
        String[] clsStr = getStringArray(name);
        Class[] ret = new Class[clsStr.length];
        try {
            for (int i = 0; i < clsStr.length; i++) {
                ret[i] = Class.forName(clsStr[i]);
            }
            return ret;

        } catch (ClassNotFoundException e) {
            e.printStackTrace(System.err);
        }
        return null;
    }

    private void listJars(List<String> list, File dir) {
        File[] files = dir.listFiles();
        // plugin ディレクトリをなくしたので
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                listJars(list, file);
            } else if (file.isFile()) {
                String path = file.getPath();
                if (path.toLowerCase().endsWith(".jar")) {
                    list.add(path);
                }
            }
        }
    }

    /**
     * Windows のデフォルトフォントを設定する.
     */
    private void setUIFonts() {

        if (isWin() || isLinux()) {
            int size = 12;
            if (isLinux()) {
                size = 13;
            }
            Font font = new Font("SansSerif", Font.PLAIN, size);
            UIManager.put("Label.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("ToggleButton.font", font);
            UIManager.put("Menu.font", font);
            UIManager.put("MenuItem.font", font);
            UIManager.put("CheckBox.font", font);
            UIManager.put("CheckBoxMenuItem.font", font);
            UIManager.put("RadioButton.font", font);
            UIManager.put("RadioButtonMenuItem.font", font);
            UIManager.put("ToolBar.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("TabbedPane.font", font);
            UIManager.put("TitledBorder.font", font);
            UIManager.put("List.font", font);

            getBootLogger().info("デフォルトのフォントを変更しました");
        }
    }
}
