package sqlg2.db.server;

import sqlg2.db.*;

import java.sql.Connection;
import java.sql.SQLException;

abstract class AbstractTransaction extends InternalTransaction implements ISimpleTransaction {

    private final SQLGLogger logger;
    private final ServerGlobals globals;
    private final ConnectionManager cman;
    private Connection conn = null;
    private final Object connLock = new Object();

    protected AbstractTransaction(SQLGLogger logger, ServerGlobals globals, ConnectionManager cman) {
        this.logger = logger;
        this.globals = globals;
        this.cman = cman;
    }

    public final <T extends IDBCommon> T getInterface(Class<T> iface) {
        return getInterface(iface, false);
    }

    public final Connection getConnection() throws SQLException {
        synchronized (connLock) {
            if (conn == null) {
                conn = cman.allocConnection();
            }
            return conn;
        }
    }

    protected final void doCommit() throws SQLException {
        synchronized (connLock) {
            if (conn != null) {
                try {
                    cman.commit(conn);
                } catch (SQLException ex) {
                    try {
                        cman.rollback(conn);
                    } catch (SQLException ex2) {
                        logger.error(ex2);
                    }
                    throw ex;
                } finally {
                    cman.releaseConnection(conn);
                    conn = null;
                }
            }
        }
    }

    protected final void doRollback() throws SQLException {
        synchronized (connLock) {
            if (conn != null) {
                try {
                    cman.rollback(conn);
                } finally {
                    cman.releaseConnection(conn);
                    conn = null;
                }
            }
        }
    }

    public final Caches getCaches() {
        return globals.caches;
    }

    public final DBSpecific getSpecific() {
        return globals.specific;
    }

    public final SqlTrace getSqlTrace() {
        return globals.trace;
    }

    public final SQLGLogger getLogger() {
        return logger;
    }
}
