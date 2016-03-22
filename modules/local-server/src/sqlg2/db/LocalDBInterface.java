package sqlg2.db;

import sqlg2.db.server.ServerGlobals;
import sqlg2.db.server.SimpleTransaction;
import sqlg2.db.server.Transaction;

import java.sql.SQLException;

public final class LocalDBInterface implements IDBInterface {

    final ConnectionManager cman;
    final ServerGlobals globals;

    public LocalDBInterface(ConnectionManager cman, DBSpecific specific, SQLGLogger logger) {
        this.cman = cman;
        this.globals = new ServerGlobals(specific, logger);
    }

    public ISimpleTransaction getSimpleTransaction() {
        return new SimpleTransaction(globals, cman);
    }

    public ITransaction getTransaction() {
        return new Transaction(globals, cman);
    }

    public void close() {
        try {
            cman.close();
        } catch (SQLException ex) {
            globals.getLogger().error(ex);
        }
    }

    public void setSqlTrace(SqlTrace trace) {
        globals.setSqlTrace(trace);
    }
}
