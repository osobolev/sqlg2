package sqlg2.db;

import sqlg2.db.remote.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Server-side object for HTTP access to business interfaces.
 * Method {@link #dispatch(String, InputStream, OutputStream)} should be invoked from servlets.
 * <p>
 * Servlet container can have more than one {@link HttpDispatcher} object, and distinguish them by
 * application name (which should be reflected in servlet URL; for example, servlet on
 * /app1/remoting path invokes HttpDispatcher of application app1, etc).
 * <p>
 * {@link HttpDispatcher} object should be created only once for application.
 */
public final class HttpDispatcher {

    private final String application;
    private final LocalConnectionFactory lw;
    private final WatcherThread watcher;
    private final ConcurrentMap<Long, ITransaction> transactions = new ConcurrentHashMap<Long, ITransaction>();

    private final AtomicLong transactionCount = new AtomicLong(0);

    private abstract static class HttpAction {

        abstract Object perform(HttpId id, String hostName, Object... params) throws Exception;
    }

    private final EnumMap<HttpCommand, HttpAction> actions = new EnumMap<HttpCommand, HttpAction>(HttpCommand.class);

    {
        actions.put(HttpCommand.OPEN, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) throws SQLException {
                checkApplication(id);
                String user = (String) params[0];
                String password = (String) params[1];
                return openConnection(id, user, password, hostName);
            }
        });
        actions.put(HttpCommand.GET_SESSIONS, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) {
                checkSession(id);
                return lw.getActiveSessions();
            }
        });
        actions.put(HttpCommand.GET_TRANSACTION, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) {
                DBInterface db = checkSession(id);
                ITransaction trans = db.getTransaction();
                long transactionId = transactionCount.getAndIncrement();
                transactions.put(transactionId, trans);
                return new HttpTransaction(id.createTransaction(transactionId), null);
            }
        });
        actions.put(HttpCommand.PING, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) {
                DBInterface db = checkSession(id);
                db.ping();
                db.tracePing();
                return null;
            }
        });
        actions.put(HttpCommand.CLOSE, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) {
                DBInterface db = checkSession(id);
                db.close();
                return null;
            }
        });
        actions.put(HttpCommand.KILL_SESSION, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) {
                checkSession(id);
                String sessionLongId = (String) params[0];
                lw.killSession(sessionLongId);
                return null;
            }
        });
        actions.put(HttpCommand.GET_CURRENT_SESSION, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) {
                DBInterface db = checkSession(id);
                return lw.getSessionInfo(db);
            }
        });
        actions.put(HttpCommand.ROLLBACK, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) throws SQLException {
                if (id.transactionId == null)
                    throw new RemoteException("Cannot call method: rollback");
                checkSession(id);
                ITransaction transaction = transactions.get(id.transactionId);
                if (transaction == null)
                    throw new RemoteException("Cannot rollback - transaction inactive: " + id.transactionId);
                transaction.rollback();
                freeTransaction(id);
                return null;
            }
        });
        actions.put(HttpCommand.COMMIT, new HttpAction() {
            Object perform(HttpId id, String hostName, Object... params) throws SQLException {
                if (id.transactionId == null)
                    throw new RemoteException("Cannot call method: commit");
                checkSession(id);
                ITransaction transaction = transactions.get(id.transactionId);
                if (transaction == null)
                    throw new RemoteException("Cannot commit - transaction inactive: " + id.transactionId);
                transaction.commit();
                freeTransaction(id);
                return null;
            }
        });
    }

    public HttpDispatcher(String application, SessionFactory sessionFactory, DBSpecific specific) {
        this(application, sessionFactory, specific, null);
    }

    public HttpDispatcher(String application, SessionFactory sessionFactory, DBSpecific specific, SQLGLogger logger) {
        this.application = application;
        this.lw = new LocalConnectionFactory(sessionFactory, specific, logger, true);
        this.watcher = new WatcherThread(1, new Runnable() {
            public void run() {
                lw.checkActivity();
            }
        });
        this.watcher.runThread();
    }

    private HttpDBInterface openConnection(HttpId id, String user, String password, String hostName) throws SQLException {
        DBInterface db = lw.createConnection(user, password, hostName, false);
        String sessionId = db.sessionLongId;
        String login = db.getUserLogin();
        String host = db.getUserHost();
        Object userObject = db.getUserObject();
        return new HttpDBInterface(id.createSession(sessionId), null, login, host, userObject);
    }

    private void checkApplication(HttpId id) {
        if (!application.equals(id.application))
            throw new RemoteException("Wrong application");
    }

    private DBInterface checkSession(HttpId id) {
        checkApplication(id);
        if (id.sessionId == null)
            throw new RemoteException("Invalid session");
        DBInterface db = lw.getSession(id.sessionId);
        if (db == null)
            throw new RemoteException("Session closed");
        return db;
    }

    private void freeTransaction(HttpId id) {
        transactions.remove(id.transactionId);
    }

    @SuppressWarnings("unchecked")
    private Object dispatch(ObjectInputStream ois, String hostName) throws Throwable {
        DBInterface db = null;
        try {
            HttpId id = (HttpId) ois.readObject();
            if (id.sessionId != null) {
                db = checkSession(id);
            }
            HttpCommand command = (HttpCommand) ois.readObject();
            Class<? extends IDBCommon> iface = (Class<? extends IDBCommon>) ois.readObject();
            String method = (String) ois.readObject();
            Class<?>[] paramTypes = (Class<?>[]) ois.readObject();
            Object[] params = (Object[]) ois.readObject();
            if (command == HttpCommand.INVOKE || command == HttpCommand.INVOKE_ASYNC) {
                Object impl;
                if (id.transactionId != null) {
                    if (command == HttpCommand.INVOKE_ASYNC)
                        throw new RemoteException("Async calls are not supported inside transaction");
                    ITransaction transaction = transactions.get(id.transactionId);
                    if (transaction == null)
                        throw new RemoteException("Transaction inactive: " + id.transactionId);
                    impl = transaction.getInterface(iface);
                } else {
                    assert db != null;
                    ISimpleTransaction t = command == HttpCommand.INVOKE_ASYNC ? db.getAsyncTransaction() : db.getSimpleTransaction();
                    impl = t.getInterface(iface);
                }
                Method toInvoke = impl.getClass().getMethod(method, paramTypes);
                try {
                    return toInvoke.invoke(impl, params);
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
            } else {
                return actions.get(command).perform(id, hostName, params);
            }
        } catch (RemoteException ex) {
            throw ex;
        } catch (Throwable ex) {
            if (db != null) {
                db.getLogger().error(ex);
            } else {
                lw.globals.getLogger().error(ex);
            }
            throw ex;
        }
    }

    /**
     * Dispatch of HTTP PUT request.
     *
     * @param hostName host name of client from which call originated
     * @param is input data
     * @param os output data
     */
    public void dispatch(String hostName, InputStream is, OutputStream os) throws IOException {
        ObjectInputStream ois = HttpId.readData(is);
        Object result = null;
        Throwable error = null;
        try {
            result = dispatch(ois, hostName);
        } catch (Throwable ex) {
            error = ex;
        }
        writeResponse(os, result, error);
    }

    public static void writeResponse(OutputStream os, Object result, Throwable error) throws IOException {
        ObjectOutputStream oos = HttpId.writeData(os);
        oos.writeObject(result);
        oos.writeObject(error);
        oos.close();
    }

    /**
     * Server shutdown
     */
    public void shutdown() {
        watcher.shutdown();
    }

    public SessionInfo[] getActiveSessions() {
        return lw.getActiveSessions();
    }

    public String getApplication() {
        return application;
    }

    public void setSqlTrace(SqlTrace trace) {
        lw.setSqlTrace(trace);
    }
}
