package sqlg2.db.client;

import sqlg2.db.remote.HttpResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class DefaultHttpClient implements IHttpClient, IClientSerializer.ReqRespProcessor {

    private final HttpURLConnection conn;
    private boolean connected = false;

    public DefaultHttpClient(HttpURLConnection conn) {
        this.conn = conn;
    }

    public static DefaultHttpClient create(URL url, Proxy proxy, int connectTimeout) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection(proxy);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(connectTimeout);
        conn.setUseCaches(false);
        conn.setAllowUserInteraction(false);
        return new DefaultHttpClient(conn);
    }

    public IClientSerializer.ReqRespProcessor getProcessor() {
        return this;
    }

    public HttpResult process(IClientSerializer.ReqRespConsumer consumer) throws IOException {
        connected = true;
        conn.connect();
        OutputStream os = conn.getOutputStream();
        try {
            consumer.writeToServer(os);
        } finally {
            os.close();
        }
        InputStream is = conn.getInputStream();
        try {
            return consumer.readFromServer(is);
        } finally {
            is.close();
        }
    }

    public void close() throws IOException {
        if (connected) {
            conn.disconnect();
        }
    }
}
