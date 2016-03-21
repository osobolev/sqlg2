package sqlg2.server.common;

import sqlg2.db.ConnectionManager;
import sqlg2.db.ITransaction;

import java.sql.SQLException;

public final class Transaction extends AbstractTransaction implements ITransaction {

    public Transaction(ServerGlobals globals, ConnectionManager cman) {
        super(globals, cman);
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
