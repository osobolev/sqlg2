package sqlg2.checker;

import sqlg2.db.specific.OracleDBSpecific;
import sqlg2.queries.QueryParser;

import java.sql.*;

public final class OracleSqlChecker implements SqlChecker {

    public String getCurrentSchema(DatabaseMetaData meta) throws SQLException {
        return meta.getUserName();
    }

    public void checkSequenceExists(Connection conn, String name) throws SQLException {
        checkSql(conn, OracleDBSpecific.getNextSeqSql(name));
    }

    public void checkSql(Connection conn, String sql) throws SQLException {
        String trim = sql.trim().toUpperCase();
        if (trim.startsWith("CREATE") || trim.startsWith("ALTER") || trim.startsWith("DROP"))
            return;
        if (trim.startsWith("{") || trim.startsWith("BEGIN") || trim.startsWith("DECLARE")) {
            // todo
        } else {
            CallableStatement cs = null;
            try {
                String txt =
                    "DECLARE\n" +
                    " c NUMBER;\n" +
                    "BEGIN\n" +
                    " c := DBMS_SQL.open_cursor;\n" +
                    " DBMS_SQL.parse(c, ?, DBMS_SQL.native);\n" +
                    " DBMS_SQL.close_cursor(c);\n" +
                    "EXCEPTION WHEN OTHERS THEN\n" +
                    " DBMS_SQL.close_cursor(c);\n" +
                    " RAISE;\n" +
                    "END;";
                cs = conn.prepareCall(txt);
                cs.setString(1, QueryParser.unparseQuery(sql));
                cs.execute();
            } finally {
                if (cs != null) {
                    try {
                        cs.close();
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
            }
        }
    }

    public void checkStatement(PreparedStatement stmt) {
    }
}
