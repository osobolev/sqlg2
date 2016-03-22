package sqlg2.checker;

import sqlg2.db.specific.PgDBSpecific;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQL checker for PostgreSQL
 */
public final class PgSqlChecker extends NoSqlChecker {

    public void checkSequenceExists(Connection conn, String name) throws SQLException {
        checkSql(conn, PgDBSpecific.getNextSeqSql(name));
    }
}
