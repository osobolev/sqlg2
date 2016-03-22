package sqlg2.db.remote;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class HttpId implements Serializable {

    public final String application;
    public final String sessionId;
    public final Long transactionId;

    public HttpId(String application) {
        this(application, null, null);
    }

    private HttpId(String application, String sessionId, Long transactionId) {
        this.application = application;
        this.sessionId = sessionId;
        this.transactionId = transactionId;
    }

    public HttpId createSession(String sessionId) {
        return new HttpId(application, sessionId, null);
    }

    public HttpId createTransaction(long transactionId) {
        return new HttpId(application, sessionId, transactionId);
    }

    public static ObjectInputStream readData(InputStream is) throws IOException {
        return new ObjectInputStream(new GZIPInputStream(is));
    }

    public static ObjectOutputStream writeData(OutputStream os) throws IOException {
        return new ObjectOutputStream(new GZIPOutputStream(os));
    }
}
