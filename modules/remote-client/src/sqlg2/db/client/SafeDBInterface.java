package sqlg2.db.client;

import sqlg2.db.*;
import sqlg2.db.remote.WatcherThread;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;

/**
 * Wrapper for {@link IDBInterface} on the client side.
 * Caches transactions - creates only one transaction per thread.
 * Also runs activity notification thread to notify server about this client.
 */
public final class SafeDBInterface implements IRemoteDBInterface {

    private IRemoteDBInterface idb;
    private final ConnectionProducer producer;
    private int resetCounter = 0;
    private boolean unrecoverable = false;

    private final WatcherThread watcher;

    public SafeDBInterface(SQLGLogger logger, ConnectionProducer producer) throws Exception {
        this(logger, producer.open(), producer);
    }

    /**
     * Constructor.
     *
     * @param idb DB connection
     */
    public SafeDBInterface(SQLGLogger logger, IRemoteDBInterface idb) {
        this(logger, idb, null);
    }

    public SafeDBInterface(final SQLGLogger logger, IRemoteDBInterface idb, ConnectionProducer producer) {
        this.idb = idb;
        this.producer = producer;
        // pinging twice as frequent as server checks session activity
        this.watcher = new WatcherThread(2, new Runnable() {
            public void run() {
                try {
                    ping();
                } catch (RemoteException ex) {
                    logger.error(ex);
                }
            }
        });
        this.watcher.runThread();
    }

    private IRemoteDBInterface getDb() {
        synchronized (this) {
            if (idb == null && producer != null) {
                if (unrecoverable)
                    throw new RemoteException("Unrecoverable error, please restart application");
                try {
                    idb = producer.open();
                } catch (RemoteException rex) {
                    throw rex;
                } catch (Exception ex) {
                    throw new RemoteException(ex);
                }
            }
            return idb;
        }
    }

    ISimpleTransaction createSimpleTransaction() throws SQLException {
        return getDb().getSimpleTransaction();
    }

    public ISimpleTransaction getSimpleTransaction() throws SQLException {
        if (producer == null) {
            return createSimpleTransaction();
        } else {
            return new SafeSimpleTransaction(this);
        }
    }

    public ISimpleTransaction getAsyncTransaction() throws SQLException {
        return getDb().getAsyncTransaction();
    }

    public ITransaction getTransaction() throws SQLException {
        return getDb().getTransaction();
    }

    public void ping() {
        getDb().ping();
    }

    public void close() {
        watcher.shutdown();
        synchronized (this) {
            if (idb != null) {
                idb.close();
                idb = null;
            }
        }
    }

    public String getUserLogin() {
        return getDb().getUserLogin();
    }

    public String getUserHost() {
        return getDb().getUserHost();
    }

    public Object getUserObject() {
        return getDb().getUserObject();
    }

    public SessionInfo[] getActiveSessions() {
        return getDb().getActiveSessions();
    }

    public void killSession(String sessionLongId) {
        getDb().killSession(sessionLongId);
    }

    public SessionInfo getCurrentSession() {
        return getDb().getCurrentSession();
    }

    @SuppressWarnings("unchecked")
    <T extends IDBCommon> T wrap(Class<T> iface, final SafeWrapper<T> obj) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                try {
                    return method.invoke(obj.get(), args);
                } catch (InvocationTargetException itex) {
                    Throwable ex = itex.getTargetException();
                    if (!(ex instanceof InformationException)) {
                        resetConnection(ex instanceof UnrecoverableRemoteException);
                    }
                    throw ex;
                }
            }
        });
    }

    private void resetConnection(boolean unrecoverable) {
        synchronized (this) {
            if (idb != null) {
                try {
                    idb.close();
                } catch (RemoteException ex) {
                    // ignore
                }
                idb = null;
                resetCounter++;
                this.unrecoverable = unrecoverable;
            }
        }
    }

    int getResetCounter() {
        synchronized (this) {
            return resetCounter;
        }
    }
}
