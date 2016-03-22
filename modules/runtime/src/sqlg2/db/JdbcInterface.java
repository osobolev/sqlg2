package sqlg2.db;

import java.sql.Connection;

/**
 * This class can be used to call SQLG code from JDBC code. If you have {@link Connection} instance then
 * you can retrieve business interface <code>ITest</code> in the following way:
 * <pre>
 * Connection conn = ...;
 * ISimpleTransaction trans = new JdbcInterface(conn, new OracleDBSpecific());
 * ITest iface = trans.getInterface(ITest.class);
 * </pre>
 * Returned implementaion will call {@link #commitImmediate()} or {@link #rollbackImmediate()} methods
 * after business method invocation success or failure respectively. By default these methods do nothing,
 * and you can override them to control transactional behaviour of business methods. Other than that
 * no transaction control is performed, so you should commit/rollback or use
 * auto-commit on {@link Connection} yourself.
 */
public class JdbcInterface extends InternalTransaction implements ISimpleTransaction {

    private final Caches caches;

    private final Connection conn;
    private final DBSpecific specific;
    private final SqlTrace sqlTrace;
    private final SQLGLogger logger;

    public JdbcInterface(Connection conn, DBSpecific specific, SQLGLogger logger) {
        this(new Caches(), conn, specific, null, logger);
    }

    public JdbcInterface(Caches caches, Connection conn, DBSpecific specific, SqlTrace sqlTrace, SQLGLogger logger) {
        this.caches = caches;
        this.conn = conn;
        this.specific = specific;
        this.sqlTrace = sqlTrace == null ? SqlTrace.DEFAULT_TRACE : sqlTrace;
        this.logger = logger;
    }

    public Connection getConnection() {
        return conn;
    }

    public void commitImmediate() {
    }

    public void rollbackImmediate() {
    }

    public final Caches getCaches() {
        return caches;
    }

    public final DBSpecific getSpecific() {
        return specific;
    }

    public final SqlTrace getSqlTrace() {
        return sqlTrace;
    }

    public SQLGLogger getLogger() {
        return logger;
    }

    public final <T extends IDBCommon> T getInterface(Class<T> iface) {
        return getInlineInterface(iface);
    }
}
