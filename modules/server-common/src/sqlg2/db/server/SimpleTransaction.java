package sqlg2.db.server;

import sqlg2.db.ConnectionManager;
import sqlg2.db.SQLGLogger;

import java.sql.SQLException;

public final class SimpleTransaction extends AbstractTransaction {

    public SimpleTransaction(SQLGLogger logger, ServerGlobals globals, ConnectionManager cman) {
        super(logger, globals, cman);
    }

    public void commitImmediate() throws SQLException {
        doCommit();
    }

    public void rollbackImmediate() throws SQLException {
        doRollback();
    }
}
