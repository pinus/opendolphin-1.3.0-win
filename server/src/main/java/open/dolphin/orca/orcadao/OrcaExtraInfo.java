package open.dolphin.orca.orcadao;

import open.dolphin.orca.OrcaHostInfo;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * API で取れない ORCA の情報を保持.
 *
 * @author pns
 */
public class OrcaExtraInfo {

    private int hospNum = 1;
    private String dbVersion = "";
    private Map<String, String> kanricd1012 = new HashMap<>();

    private Logger logger = Logger.getLogger(OrcaDao.class);

    public OrcaExtraInfo() {
        init();
    }

    private void init() {
        //
        // hospNum
        //
        String sql = "select hospnum, kanritbl from tbl_syskanri where kanricd='1001' and kanritbl ~ ?";
        OrcaDbConnection con = new OrcaDbConnection(rs -> {
            if (rs.next()) {
                hospNum = rs.getInt(1);
            }
        });
        con.setParam(1, OrcaHostInfo.getInstance().getJmariCode());
        con.executeQuery(sql);

        //
        // dbVersion
        //
        sql = "select version from tbl_dbkanri where kanricd='ORCADB00'";
        con = new OrcaDbConnection(rs -> {
            if (rs.next()) {
                dbVersion = rs.getString(1);
            }
        });
        con.executeQuery(sql);

        //
        // 診療内容情報 (tbl_syskanri kanricd=1012) 診察1, 診察2, etc.
        // 1012    | 04       | 水いぼ                                  水いぼ                  0
        // 1012    | 06       | 健康診断                                健康診断
        // 1012    | 07       | 予防注射                                予防注射
        //
        sql = "select kbncd, kanritbl from tbl_syskanri where hospnum = ? and kanricd = ?";
        con = new OrcaDbConnection(rs -> {
            while (rs.next()) {
                String kbncd = rs.getString(1).trim();
                String[] item = rs.getString(2).split(" +");
                kanricd1012.put(kbncd, item[1]);
            }
        });
        con.setParam(1, hospNum);
        con.setParam(2, "1012");
        con.executeQuery(sql);

        logger.info("hospNum = " + hospNum);
        logger.info("dbVersion = " + dbVersion);

    }

    /**
     * HospNum を返す.
     *
     * @return hospNum
     */
    public int getHospNum() {
        return hospNum;
    }

    /**
     * ORCA の Database Version を返す.
     *
     * @return dbVersion
     */
    public String getDbVersion() {
        return dbVersion;
    }

    /**
     * システム管理 kanricd 1012 診療内容情報 (診察1, 診察2, etc) を返す.
     *
     * @return kanricd1012
     */
    public Map<String, String> getKanricd1012() {
        return kanricd1012;
    }

}
