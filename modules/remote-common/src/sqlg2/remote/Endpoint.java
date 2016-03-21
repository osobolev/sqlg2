package sqlg2.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

final class Endpoint implements Serializable {

    private URL url;
    private Proxy proxy;

    Endpoint(URL url, Proxy proxy) {
        this.url = url;
        this.proxy = proxy;
    }

    HttpURLConnection openConnection() throws IOException {
        return (HttpURLConnection) url.openConnection(proxy);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(url);
        out.writeObject(proxy.type());
        out.writeObject(proxy.address());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        url = (URL) in.readObject();
        Proxy.Type type = (Proxy.Type) in.readObject();
        SocketAddress address = (SocketAddress) in.readObject();
        if (type == Proxy.Type.DIRECT) {
            proxy = Proxy.NO_PROXY;
        } else {
            proxy = new Proxy(type, address);
        }
    }

    URL getUrl() {
        return url;
    }
}
