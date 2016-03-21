package sqlg2.checker;

import java.sql.*;

public class NoSqlChecker implements SqlChecker {

    public String getCurrentSchema(DatabaseMetaData meta) throws SQLException {
        return meta.getUserName();
    }

    public void checkSequenceExists(Connection conn, String name) throws SQLException {
        throw new SQLException("Database does not support sequences");
    }

    public void checkSql(Connection conn, String sql) throws SQLException {
        String trim = sql.trim().toUpperCase();
        if (trim.startsWith("CREATE") || trim.startsWith("ALTER") || trim.startsWith("DROP"))
            return;
        if (trim.startsWith("{")) {
            CallableStatement cs = null;
            try {
                cs = conn.prepareCall(sql);
                checkStatement(cs);
            } finally {
                if (cs != null) {
                    try {
                        cs.close();
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
            }
        } else {
            PreparedStatement ps = null;
            try {
                ps = conn.prepareStatement(sql);
                checkStatement(ps);
            } finally {
                if (ps != null) {
                    try {
                        ps.close();
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
            }
        }
    }

    public void checkStatement(PreparedStatement stmt) throws SQLException {
        stmt.getParameterMetaData();
    }
}
