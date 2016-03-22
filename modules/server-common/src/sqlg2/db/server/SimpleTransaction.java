package sqlg2.db.server;

import sqlg2.db.ConnectionManager;

import java.sql.SQLException;

public final class SimpleTransaction extends AbstractTransaction {

    public SimpleTransaction(ServerGlobals globals, ConnectionManager cman) {
        super(globals, cman);
    }

    public void commitImmediate() throws SQLException {
        doCommit();
    }

    public void rollbackImmediate() throws SQLException {
        doRollback();
    }
}
