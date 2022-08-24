package open.dolphin.orca.orcadao;

import open.dolphin.orca.OrcaHostInfo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;

/**
 * OrcaDbConnection.
 * ORCA Database への接続，Statement での query 実行をまとめて行う.
 *
 * @author pns
 */
public class OrcaDbConnection {

    private String url = OrcaHostInfo.getInstance().getJdbcUrl();
    private Properties props = OrcaHostInfo.getInstance().getJdbcProperties();

    private ResultSetProcessor resultSetProcessor;
    private HashMap<Integer, Object> paramMap = new HashMap<>();

    public OrcaDbConnection(ResultSetProcessor rsp) {
        resultSetProcessor = rsp;
    }

    /**
     * PreparedStatement で query を実行する.
     *
     * @param sql Query
     */
    public void executeQuery(String sql) {
        try (Connection con = DriverManager.getConnection(url, props);
             PreparedStatement st = con.prepareStatement(sql)) {

            setParam(st);
            resultSetProcessor.process(st.executeQuery());

        } catch (SQLException e) {
            OrcaDao.getInstance().processError(e);
        }
    }

    /**
     * PreparedStatement に対して parameter をセットする.
     *
     * @param st PreparedStatement
     * @throws SQLException SQLException
     */
    private void setParam(PreparedStatement st) throws SQLException {
        for (int index : paramMap.keySet()) {
            Object value = paramMap.get(index);

            switch (value.getClass().getName()) {
                case "java.lang.String":
                    st.setString(index, (String) value);
                    break;

                case "java.lang.Integer":
                    st.setInt(index, (Integer) value);
                    break;

                case "java.lang.Float":
                    st.setFloat(index, (Float) value);
            }
        }
    }

    /**
     * PreparedStatement のための parameter をセットする.
     *
     * @param paramIndex index of the parameter
     * @param object     parameter
     */
    public void setParam(int paramIndex, Object object) {
        paramMap.put(paramIndex, object);
    }
}
