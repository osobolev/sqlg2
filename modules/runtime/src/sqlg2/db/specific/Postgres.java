package sqlg2.db.specific;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link sqlg2.db.DBSpecific} implementation for PostgreSQL.
 */
public final class Postgres extends Generic {

    public static String getNextSeqSql(String sequence) {
        return "SELECT NEXTVAL('" + sequence + "')";
    }

    public long getNextId(Connection conn, String sequence) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(getNextSeqSql(sequence)); ResultSet rs = stmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        }
        // ignore
        // ignore
    }

    public String getCheckerClassName() {
        return "sqlg2.checker.Postgres";
    }
}
