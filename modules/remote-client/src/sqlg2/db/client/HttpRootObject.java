package sqlg2.db.client;

import sqlg2.db.IDBCommon;
import sqlg2.db.RemoteException;
import sqlg2.db.remote.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

final class HttpRootObject {

    private final URL url;
    private final Proxy proxy;
    private ISerializer serializer = new JavaSerializer();

    HttpRootObject(URL url, Proxy proxy) {
        this.url = url;
        this.proxy = proxy;
    }

    void setSerializer(ISerializer serializer) {
        this.serializer = serializer;
    }

    @SuppressWarnings("unchecked")
    <T> T httpInvoke(Class<T> retType, HttpCommand command, HttpId id, Object... params) throws Throwable {
        return (T) httpInvoke(retType, command, id, null, null, null, params);
    }

    URL getUrl() {
        return url;
    }

    private HttpURLConnection getConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(3000);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        return conn;
    }

    Object httpInvoke(Type retType, HttpCommand command, HttpId id, Class<? extends IDBCommon> iface, String method, Class<?>[] paramTypes, Object[] params) throws Throwable {
        Object result;
        Throwable error;
        try {
            final HttpURLConnection conn = getConnection();
            try {
                conn.connect();
                ISerializer.StreamSource<OutputStream> oss = new ISerializer.StreamSource<OutputStream>() {
                    public OutputStream open() throws IOException {
                        return conn.getOutputStream();
                    }
                };
                ISerializer.StreamSource<InputStream> iss = new ISerializer.StreamSource<InputStream>() {
                    public InputStream open() throws IOException {
                        return conn.getInputStream();
                    }
                };
                HttpResult httpResult = serializer.clientToServer(oss, id, command, iface, retType, method, paramTypes, params, iss);
                result = httpResult.result;
                error = httpResult.error;
            } finally {
                conn.disconnect();
            }
        } catch (RuntimeException ex) {
            throw ex;
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
