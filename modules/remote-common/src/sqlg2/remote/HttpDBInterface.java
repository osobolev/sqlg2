package sqlg2.remote;

import sqlg2.db.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

public final class HttpDBInterface extends HttpProxy implements IRemoteDBInterface {

    private final String userLogin;
    private final String userHost;
    private final Object userObject;

    public HttpDBInterface(HttpId id, HttpProxy root, String userLogin, String userHost, Object userObject) {
        super(id, root);
        this.userLogin = userLogin;
        this.userHost = userHost;
        this.userObject = userObject;
    }

    public ISimpleTransaction getSimpleTransaction() {
        return new HttpSimpleTransaction(id, this, HttpCommand.INVOKE);
    }

    public ISimpleTransaction getAsyncTransaction() {
        return new HttpSimpleTransaction(id, this, HttpCommand.INVOKE_ASYNC);
    }

    public ITransaction getTransaction() throws SQLException {
        try {
            HttpTransaction trans = (HttpTransaction) httpInvoke(HttpCommand.GET_TRANSACTION);
            trans.setEndpoint(this);
            return trans;
        } catch (SQLException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    public void ping() {
        try {
            httpInvoke(HttpCommand.PING);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    public void close() {
        try {
            httpInvoke(HttpCommand.CLOSE);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserHost() {
        return userHost;
    }

    public Object getUserObject() {
        return userObject;
    }

    public SessionInfo[] getActiveSessions() {
        try {
            return (SessionInfo[]) httpInvoke(HttpCommand.GET_SESSIONS);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    public void killSession(String sessionLongId) {
        try {
            httpInvoke(HttpCommand.KILL_SESSION, sessionLongId);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    public SessionInfo getCurrentSession() {
        try {
            return (SessionInfo) httpInvoke(HttpCommand.GET_CURRENT_SESSION);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(getEndpoint());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Endpoint endpoint = (Endpoint) in.readObject();
        setEndpoint(endpoint);
    }
}
