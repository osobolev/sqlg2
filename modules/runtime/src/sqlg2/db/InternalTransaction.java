package sqlg2.db;

import sqlg2.LocalWrapperBase;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentMap;

/**
 * For internal use.
 * Ties SQLG runtime library with user code.
 */
public abstract class InternalTransaction {

    public abstract Connection getConnection() throws SQLException;

    public abstract void commitImmediate() throws SQLException;

    public abstract void rollbackImmediate() throws SQLException;

    public abstract Caches getCaches();

    public abstract DBSpecific getSpecific();

    public abstract SqlTrace getSqlTrace();

    public abstract SQLGLogger getLogger();

    private Constructor<?> getConstructor(Class<? extends IDBCommon> iface) throws ClassNotFoundException, NoSuchMethodException {
        ConcurrentMap<Class<?>, Constructor<?>> cache = getCaches().getWrapperMap();
        Constructor<?> o = cache.get(iface);
        if (o != null) {
            return o;
        } else {
            String name = LocalWrapperBase.getWrapperNameFromInterface(iface);
            Class<?> cls = LocalWrapperBase.loadClass(name);
            Constructor<?> cons = cls.getConstructor(InternalTransaction.class, Boolean.TYPE);
            cache.put(iface, cons);
            return cons;
        }
    }

    private Object getInstance(Class<? extends IDBCommon> iface, boolean inline) {
        try {
            Constructor<?> cons = getConstructor(iface);
            return cons.newInstance(this, inline);
        } catch (Exception ex) {
            throw new SQLGException("Wrapper for " + iface.getCanonicalName() + " not found", ex);
        }
    }

    /**
     * Returns local business interface implementation for current transaction
     */
    @SuppressWarnings("unchecked")
    protected final <T extends IDBCommon> T getInterface(Class<T> iface, boolean inline) {
        return (T) getInstance(iface, inline);
    }

    public final <T extends IDBCommon> T getInlineInterface(Class<T> iface) {
        return getInterface(iface, true);
    }
}
