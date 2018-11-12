package sqlg2.db;

import sqlg2.db.remote.BaseJavaSerializer;
import sqlg2.db.remote.HttpCommand;
import sqlg2.db.remote.HttpId;

import java.io.*;

public final class ServerJavaSerializer extends BaseJavaSerializer implements IServerSerializer {

    public ServerJavaSerializer(SQLGLogger logger, boolean onlyMethods) {
        super(logger, onlyMethods);
    }

    public ServerJavaSerializer() {
    }

    @SuppressWarnings("unchecked")
    public void serverToClient(InputStream is, ServerCall call, OutputStream os) throws IOException {
        HttpId id;
        HttpCommand command;
        Class<? extends IDBCommon> iface;
        String method;
        Class<?>[] paramTypes;
        Object[] params;
        ObjectInputStream ois = readData(count(is, true));
        try {
            id = (HttpId) ois.readObject();
            command = (HttpCommand) ois.readObject();
            iface = (Class<? extends IDBCommon>) ois.readObject();
            method = (String) ois.readObject();
            paramTypes = (Class<?>[]) ois.readObject();
            params = (Object[]) ois.readObject();
        } catch (ClassNotFoundException ex) {
            throw new UnrecoverableRemoteException(ex);
        } catch (InvalidClassException ex) {
            throw new UnrecoverableRemoteException(ex);
        } finally {
            ois.close();
        }
        Object result = null;
        Throwable error = null;
        try {
            result = call.call(id, command, iface, method, paramTypes, params);
        } catch (Throwable ex) {
            error = ex;
        }
        writeResponse(os, result, error, method != null);
    }

    private void writeResponse(OutputStream os, Object result, Throwable error, boolean isMethod) throws IOException {
        boolean debug = onlyMethods ? isMethod : true;
        ObjectOutputStream oos = writeData(count(os, debug));
        try {
            oos.writeObject(result);
            oos.writeObject(error);
        } finally {
            oos.close();
        }
    }

    public void sendError(OutputStream os, Throwable error) throws IOException {
        try {
            writeResponse(os, null, error, false);
        } finally {
            os.close();
        }
    }
}
