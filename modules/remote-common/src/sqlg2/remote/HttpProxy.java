package sqlg2.remote;

import sqlg2.db.RemoteException;
import sqlg2.db.UnrecoverableRemoteException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

class HttpProxy implements Serializable {

    protected final HttpId id;

    private transient Endpoint endpoint;

    HttpProxy(HttpId id, Endpoint endpoint) {
        this.id = id;
        this.endpoint = endpoint;
    }

    HttpProxy(HttpId id, HttpProxy root) {
        this.id = id;
        setEndpoint(root);
    }

    protected final void setEndpoint(HttpProxy root) {
        if (root != null) {
            this.endpoint = root.endpoint;
        }
    }

    protected final void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public final void setEndpoint(URL url, Proxy proxy) {
        this.endpoint = new Endpoint(url, proxy);
    }

    protected final Object httpInvoke(HttpCommand command, Object... params) throws Throwable {
        return httpInvoke(command, null, null, null, params);
    }

    protected final Endpoint getEndpoint() {
        return endpoint;
    }

    private HttpURLConnection getConnection() throws IOException {
        HttpURLConnection conn = endpoint.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(3000);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        return conn;
    }

    protected final Object httpInvoke(HttpCommand command, Class<?> iface, String method, Class<?>[] paramTypes, Object[] params) throws Throwable {
        Object result;
        Throwable error;
        try {
            HttpURLConnection conn = getConnection();
            try {
                conn.connect();
                OutputStream os = conn.getOutputStream();
                try {
                    ObjectOutputStream oos = HttpId.writeData(os);
                    os = oos;
                    oos.writeObject(id);
                    oos.writeObject(command);
                    oos.writeObject(iface);
                    oos.writeObject(method);
                    oos.writeObject(paramTypes);
                    oos.writeObject(params);
                } finally {
                    os.close();
                }
                InputStream is = conn.getInputStream();
                try {
                    ObjectInputStream ois = HttpId.readData(is);
                    is = ois;
                    result = ois.readObject();
                    error = (Throwable) ois.readObject();
                } finally {
                    is.close();
                }
            } finally {
                conn.disconnect();
            }
        } catch (ClassNotFoundException ex) {
            throw new UnrecoverableRemoteException(ex);
        } catch (InvalidClassException ex) {
            throw new UnrecoverableRemoteException(ex);
        } catch (Exception ex) {
            throw new RemoteException(ex);
        }
        if (error != null) {
            serverException(error);
            return null;
        } else {
            return result;
        }
    }

    private static void serverException(Throwable error) throws Throwable {
        StackTraceElement[] serverST = error.getStackTrace();
        StackTraceElement[] clientST = new Throwable().getStackTrace();
        StackTraceElement[] allST = new StackTraceElement[serverST.length + clientST.length];
        System.arraycopy(serverST, 0, allST, 0, serverST.length);
        System.arraycopy(clientST, 0, allST, serverST.length, clientST.length);
        error.setStackTrace(allST);
        throw error;
    }
}
