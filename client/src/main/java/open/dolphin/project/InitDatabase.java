package open.dolphin.project;

import open.dolphin.JsonConverter;
import open.dolphin.helper.HashUtil;
import open.dolphin.infomodel.*;
import open.dolphin.service.SystemService;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Dolphin用のマスタを登録する.
 *
 * @author Minagawa, Kazushi
 * @author pns
 */
public class InitDatabase {

    private static final String MEMBER_TYPE = "FACILITY_USER";
    private static final String DEFAULT_FACILITY_OID = IInfoModel.DEFAULT_FACILITY_OID;
    private static final String PROFIEL_RESOURCE = "/master/profiel.txt";

    // マスタデータファイル
    private static final String RAD_METHOD_RESOURCE = "/master/radiology-method-data-sjis.txt";
    private static final String ENCODING = "UTF-8";
    // マスタデータファイルの仕様
    private final int ARRAY_CAPACITY = 20;
    private final int TT_VALUE = 0;
    private final int TT_DELIM = 1;
    private final String delimitater = "\t";
    // Logger
    private final Logger logger;
    // SystemService Proxy
    private SystemService service;

    /**
     * InitDatabase のコンストラクタ
     */
    public InitDatabase() {
        logger = LoggerFactory.getLogger(InitDatabase.class);
    }

    public static void main(String[] args) throws Exception {
        final InitDatabase initDatabase = new InitDatabase();
        final String usage = "Usage: java -cp OpenDolphin-1.3.0.X.jar open.dolphin.project.InitDatabase id password hostAddress";

        if (args.length == 3) {
            initDatabase.start(args[0], args[1], args[2]);

        }
        if (args.length == 0) {
            // load data from profiel.txt
            initDatabase.start(null, null, null);
            //initDatabase.start(null, null, "http://localhost:8080/dolphin");

        } else {
            System.out.println(usage);
        }
        System.exit(0);
    }

    /**
     * Start initializing database
     *
     * @param userId user id
     * @param password password
     * @param hostAddress host address
     */
    public void start(String userId, String password, String hostAddress) {

        addDatabaseAdmin(userId, password, hostAddress);
        addDolphinMaster();

        logger.info("データベースを初期化しました。");
    }

    /**
     * Add database administrator
     *
     * @param userId user id
     * @param password password
     * @param hostAddress host address
     */
    public void addDatabaseAdmin(String userId, String password, String hostAddress) {

        HashMap<String, String> prop = new HashMap<>();

        try (InputStream in = this.getClass().getResourceAsStream(PROFIEL_RESOURCE);
             InputStreamReader ir = new InputStreamReader(in, ENCODING);
             BufferedReader reader = new BufferedReader(ir)) {

            // profiel.txt ファイルの読み込み
            String line;
            while ((line = reader.readLine()) != null) {
                // comment
                if (line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }

                // "=" があるかどうか
                int index = line.indexOf("=");
                if (index < 0) {
                    continue;
                }

                // "=" から key, value ペアを抜き出して HashMap に登録
                String key = line.substring(0, index);
                String value = line.substring(index + 1);
                String put = prop.put(key, value);
                logger.debug("detected key = " + key + ";  value = " + put);
            }

            logger.info("管理者情報ファイルを読み込みました。");

        } catch (IOException ioex) {
            logger.error("管理者情報ファイルを読み込めません。");
            ioex.printStackTrace(System.err);
            System.exit(1);
        }

        // Admin Model
        FacilityModel facility = new FacilityModel();
        UserModel adminUser = new UserModel();
        adminUser.setFacilityModel(facility);

        // 施設情報
        facility.setFacilityId(DEFAULT_FACILITY_OID);
        facility.setFacilityName(prop.get("facility.name"));
        facility.setZipCode(prop.get("facility.zipcode"));
        facility.setAddress(prop.get("facility.address"));
        facility.setTelephone(prop.get("facility.telephone"));
        facility.setUrl(prop.get("facility.url"));
        Date date = new Date();
        facility.setRegisteredDate(date);
        facility.setMemberType(MEMBER_TYPE);

        // Administrator 情報
        // userId, password が指定されていれば，profiel.txt のものと置き換える
        if (userId == null || userId.equals("")) {
            adminUser.setUserId(prop.get("admin.login.id"));
        } else {
            adminUser.setUserId(userId);
        }
        if (password == null || password.equals("")) {
            adminUser.setPassword(prop.get("admin.login.password"));
        } else {
            adminUser.setPassword(password);
        }
        // password は MD5 ハッシュして登録
        String hashPass = HashUtil.MD5(adminUser.getPassword());
        adminUser.setPassword(hashPass);
        adminUser.setSirName(prop.get("admin.sir.name"));
        adminUser.setGivenName(prop.get("admin.given.name"));
        adminUser.setCommonName(adminUser.getSirName() + " " + adminUser.getGivenName());

        // 医療資格
        LicenseModel license = new LicenseModel();
        license.setLicense("doctor");
        license.setLicenseDesc("医師");
        license.setLicenseCodeSys("MML0026");
        adminUser.setLicenseModel(license);

        // 診療科
        DepartmentModel dept = new DepartmentModel();
        dept.setDepartment("01");
        dept.setDepartmentDesc("内科");
        dept.setDepartmentCodeSys("MML0028");
        adminUser.setDepartmentModel(dept);

        // Email
        String email = prop.get("admin.email");
        if (email == null || email.equals("")) {
            adminUser.setEmail(prop.get("someone@some-clinic.jp"));
        } else {
            adminUser.setEmail(email);
        }

        // MemberTpe
        adminUser.setMemberType(MEMBER_TYPE);

        // 登録日
        adminUser.setRegisteredDate(date);

        // host address
        if (hostAddress == null) {
            hostAddress = prop.get("host.address");
        }

        // 登録
        ResteasyClient client = new ResteasyClientBuilder().build();
        client.register(new JsonConverter());
        ResteasyWebTarget target = client.target(hostAddress);
        service = target.proxy(SystemService.class);
        service.addFacilityAdmin(adminUser);

        logger.info("管理者を登録しました。");
    }

    /**
     * マスタを登録する.
     */
    public void addDolphinMaster() {
        addRdMethod(RAD_METHOD_RESOURCE);
    }

    /**
     * 放射線メソッドマスタを登録する.
     *
     * @param name 放射線メソッドマスタリソース名
     */
    private void addRdMethod(String name) {

        try (InputStream in = this.getClass().getResourceAsStream(name);
             InputStreamReader ir = new InputStreamReader(in, ENCODING);
             BufferedReader reader = new BufferedReader(ir)) {

            List<RadiologyMethodValue> list = null;

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = getStringArray(line);

                if (data != null) {

                    RadiologyMethodValue av = new RadiologyMethodValue();
                    av.setHierarchyCode1(format(data[0]));
                    av.setHierarchyCode2(format(data[1]));
                    av.setMethodName(format(data[2]));

                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(av);
                }
            }

            if (list == null) {
                return;
            }

            service.putRadMethodMaster(list);
            logger.info("放射線メソッドマスタを登録しました。");

        } catch (IOException ioex) {
            logger.error("マスターファイルを読み込めません。");
            ioex.printStackTrace(System.err);
            System.exit(1);
        } catch (RuntimeException rtex) {
            logger.error("マスターファイルの登録に失敗しました。");
            rtex.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * 文字を整形する.
     */
    private String format(String d) {
        if (d == null) {
            return null;
        } else if (d.equals("\\N")) {
            return null;
        } else {
            return d;
        }
    }

    /**
     * リソースファイルから読み込んだタブ区切りの１行をパースし， String 配列のデータにして返す.
     *
     * @param line 　パースするライン
     * @return データ配列
     */
    private String[] getStringArray(String line) {

        if (line == null) {
            return null;
        }

        String[] ret = new String[ARRAY_CAPACITY];
        int count = 0;

        StringTokenizer st = new StringTokenizer(line, delimitater, true);
        int state = TT_VALUE;

        while (st.hasMoreTokens()) {

            if ((count % ARRAY_CAPACITY) == 0) {
                String[] dest = new String[count + ARRAY_CAPACITY];
                System.arraycopy(ret, 0, dest, 0, count);
                ret = dest;
            }

            String token = st.nextToken();

            switch (state) {

                case TT_VALUE:
                    if (token.equals(delimitater)) {
                        token = null;

                    } else {
                        state = TT_DELIM;
                    }
                    ret[count] = token;
                    count++;
                    break;

                case TT_DELIM:
                    state = TT_VALUE;
                    break;
            }
        }

        String[] ret2 = new String[count];
        System.arraycopy(ret, 0, ret2, 0, count);

        return ret2;
    }
}
