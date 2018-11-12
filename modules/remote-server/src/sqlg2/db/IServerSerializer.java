package sqlg2.db;

import sqlg2.db.remote.HttpCommand;
import sqlg2.db.remote.HttpId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IServerSerializer {

    interface ServerCall {

        Object call(HttpId id, HttpCommand command, Class<? extends IDBCommon> iface, String method, Class<?>[] paramTypes, Object[] params) throws Throwable;
    }

    void serverToClient(InputStream is, ServerCall call,
                        OutputStream os) throws IOException;

    void sendError(OutputStream os, Throwable error) throws IOException;
}
