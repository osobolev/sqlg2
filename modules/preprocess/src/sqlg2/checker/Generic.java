package sqlg2.checker;

import sqlg2.SqlChecker;

import java.sql.*;

/**
 * Base SQL checker for generic JDBC database - does not support sequences.
 */
public class Generic implements SqlChecker {

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
            try (CallableStatement cs = conn.prepareCall(sql)) {
                checkStatement(cs);
            }
            // ignore
        } else {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                checkStatement(ps);
            }
            // ignore
        }
    }

    public void checkStatement(PreparedStatement stmt) throws SQLException {
        stmt.getParameterMetaData();
    }
}
