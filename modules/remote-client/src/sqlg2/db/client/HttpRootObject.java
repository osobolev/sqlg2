package sqlg2.db.client;

import sqlg2.db.IDBCommon;
import sqlg2.db.RemoteException;
import sqlg2.db.remote.HttpCommand;
import sqlg2.db.remote.HttpId;
import sqlg2.db.remote.HttpResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

final class HttpRootObject {

    private final IHttpClientFactory clientFactory;
    private IClientSerializer serializer = new ClientJavaSerializer();

    HttpRootObject(IHttpClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    void setSerializer(IClientSerializer serializer) {
        this.serializer = serializer;
    }

    @SuppressWarnings("unchecked")
    <T> T httpInvoke(Class<T> retType, HttpCommand command, HttpId id, Object... params) throws Throwable {
        return (T) httpInvoke(retType, command, id, null, null, null, params);
    }

    Object httpInvoke(Type retType, HttpCommand command, HttpId id, Class<? extends IDBCommon> iface, String method, Class<?>[] paramTypes, Object[] params) throws Throwable {
        Object result;
        Throwable error;
        try {
            final IHttpClient conn = clientFactory.getClient();
            try {
                IClientSerializer.StreamSource<OutputStream> oss = new IClientSerializer.StreamSource<OutputStream>() {
                    public OutputStream open() throws IOException {
                        return conn.toServer();
                    }
                };
                IClientSerializer.StreamSource<InputStream> iss = new IClientSerializer.StreamSource<InputStream>() {
                    public InputStream open() throws IOException {
                        return conn.fromServer();
                    }
                };
                HttpResult httpResult = serializer.clientToServer(oss, id, command, iface, retType, method, paramTypes, params, iss);
                result = httpResult.result;
                error = httpResult.error;
            } finally {
                conn.close();
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
