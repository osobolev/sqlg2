package sqlg2.db.remote;

import sqlg2.db.IDBCommon;
import sqlg2.db.SQLGLogger;
import sqlg2.db.UnrecoverableRemoteException;

import java.io.*;
import java.lang.reflect.Type;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class JavaSerializer implements ISerializer {

    private final SQLGLogger logger;
    private final boolean onlyMethods;

    public JavaSerializer(SQLGLogger logger, boolean onlyMethods) {
        this.logger = logger;
        this.onlyMethods = onlyMethods;
    }

    public JavaSerializer() {
        this(null, false);
    }

    private static ObjectOutputStream writeData(OutputStream os) throws IOException {
        return new ObjectOutputStream(new GZIPOutputStream(os));
    }

    private static ObjectInputStream readData(InputStream is) throws IOException {
        return new ObjectInputStream(new GZIPInputStream(is));
    }

    private InputStream count(InputStream is, boolean debug) throws IOException {
        if (logger != null && debug) {
            return new CountInputStream(is, logger);
        } else {
            return is;
        }
    }

    private OutputStream count(OutputStream os, boolean debug) throws IOException {
        if (logger != null && debug) {
            return new CountOutputStream(os, logger);
        } else {
            return os;
        }
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
