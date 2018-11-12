package sqlg2.db.remote;

import sqlg2.db.IDBCommon;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface ISerializer {

    interface StreamSource<S> {

        S open() throws IOException;
    }

    HttpResult clientToServer(StreamSource<OutputStream> oss, HttpId id, HttpCommand command,
                              Class<? extends IDBCommon> iface, Type retType, String method, Class<?>[] paramTypes, Object[] params,
                              StreamSource<InputStream> iss) throws IOException;

    interface ServerCall {

        Object call(HttpId id, HttpCommand command, Class<? extends IDBCommon> iface, String method, Class<?>[] paramTypes, Object[] params) throws Throwable;
    }

    void serverToClient(InputStream is, ServerCall call,
                        OutputStream os) throws IOException;

    void sendError(OutputStream os, Throwable error) throws IOException;
}
