package sqlg2.db.specific;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link sqlg2.db.DBSpecific} implementation for PostgreSQL.
 */
public final class PgDBSpecific extends NoDBSpecific {

    public static String getNextSeqSql(String sequence) {
        return "SELECT NEXTVAL('" + sequence + "')";
    }

    public long getNextId(Connection conn, String sequence) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(getNextSeqSql(sequence));
            rs = stmt.executeQuery();
            rs.next();
            return rs.getLong(1);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
        }
    }

    public String getCheckerClassName() {
        return "sqlg2.checker.PgSqlChecker";
    }
}
