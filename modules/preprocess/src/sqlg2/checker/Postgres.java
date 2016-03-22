package sqlg2.checker;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL checker for PostgreSQL
 */
public final class Postgres extends Generic {

    public void checkSequenceExists(Connection conn, String name) throws SQLException {
        checkSql(conn, sqlg2.db.specific.Postgres.getNextSeqSql(name));
    }
}
