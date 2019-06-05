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

    public HttpResult clientToServer(ReqRespProcessor processor, final HttpId id, final HttpCommand command,
                                     final Class<? extends IDBCommon> iface, Type retType, final String method, final Class<?>[] paramTypes, final Object[] params) throws IOException {
        final boolean debug = onlyMethods ? method != null : true;
        if (logger != null && debug) {
            if (method != null) {
                logger.info(iface + "." + method + ": " + retType);
            } else {
                logger.info(command + ": " + retType);
            }
        }
        return processor.process(new ReqRespConsumer() {

            public void writeToServer(OutputStream stream) throws IOException {
                OutputStream os = count(stream, debug);
                ObjectOutputStream oos = writeData(os);
                try {
                    oos.writeObject(id);
                    oos.writeObject(command);
                    oos.writeObject(iface);
                    oos.writeObject(method);
                    oos.writeObject(paramTypes);
                    oos.writeObject(params);
                } finally {
                    oos.close();
                }
            }

            public HttpResult readFromServer(InputStream stream) throws IOException {
                InputStream is = count(stream, debug);
                try {
                    ObjectInputStream ois = readData(is);
                    try {
                        Object result = ois.readObject();
                        Throwable error = (Throwable) ois.readObject();
                        return new HttpResult(result, error);
                    } finally {
                        ois.close();
                    }
                } catch (ClassNotFoundException ex) {
                    throw new UnrecoverableRemoteException(ex);
                } catch (InvalidClassException ex) {
                    throw new UnrecoverableRemoteException(ex);
                }
            }
        });
    }
}
