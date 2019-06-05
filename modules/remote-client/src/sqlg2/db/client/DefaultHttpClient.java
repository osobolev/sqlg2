package sqlg2.db.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

public class DefaultHttpClient implements IHttpClient {

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

    private void connect() throws IOException {
        if (!connected) {
            connected = true;
            conn.connect();
        }
    }

    public OutputStream toServer() throws IOException {
        connect();
        return conn.getOutputStream();
    }

    public InputStream fromServer() throws IOException {
        connect();
        return conn.getInputStream();
    }

    public void close() throws IOException {
        if (connected) {
            conn.disconnect();
        }
    }
}
