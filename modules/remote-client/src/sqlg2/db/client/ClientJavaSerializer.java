package sqlg2.db.client;

import sqlg2.db.IDBCommon;
import sqlg2.db.SQLGLogger;
import sqlg2.db.UnrecoverableRemoteException;
import sqlg2.db.remote.BaseJavaSerializer;
import sqlg2.db.remote.HttpCommand;
import sqlg2.db.remote.HttpId;
import sqlg2.db.remote.HttpResult;

import java.io.*;
import java.lang.reflect.Type;

public final class ClientJavaSerializer extends BaseJavaSerializer implements IClientSerializer {

    public ClientJavaSerializer(SQLGLogger logger, boolean onlyMethods) {
        super(logger, onlyMethods);
    }

    public ClientJavaSerializer() {
    }

    public HttpResult clientToServer(StreamSource<OutputStream> oss, HttpId id, HttpCommand command,
                                     Class<? extends IDBCommon> iface, Type retType, String method, Class<?>[] paramTypes, Object[] params,
                                     StreamSource<InputStream> iss) throws IOException {
        boolean debug = onlyMethods ? method != null : true;
        if (logger != null && debug) {
            if (method != null) {
                logger.info(iface + "." + method + ": " + retType);
            } else {
                logger.info(command + ": " + retType);
            }
        }
        OutputStream os = count(oss.open(), debug);
        try {
            ObjectOutputStream oos = writeData(os);
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
        InputStream is = count(iss.open(), debug);
        try {
            ObjectInputStream ois = readData(is);
            is = ois;
            Object result = ois.readObject();
            Throwable error = (Throwable) ois.readObject();
            return new HttpResult(result, error);
        } catch (ClassNotFoundException ex) {
            throw new UnrecoverableRemoteException(ex);
        } catch (InvalidClassException ex) {
            throw new UnrecoverableRemoteException(ex);
        } finally {
            is.close();
        }
    }
}
