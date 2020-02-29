package sqlg2.db.server;

import sqlg2.db.ConnectionManager;
import sqlg2.db.ITransaction;
import sqlg2.db.SQLGLogger;

import java.sql.SQLException;

public final class Transaction extends AbstractTransaction implements ITransaction {

    public Transaction(SQLGLogger logger, ServerGlobals globals, ConnectionManager cman) {
        super(logger, globals, cman);
    }

    public void commitImmediate() {
    }

    public void rollbackImmediate() {
    }

    public void commit() throws SQLException {
        doCommit();
    }

    public void rollback() throws SQLException {
        doRollback();
    }
}
