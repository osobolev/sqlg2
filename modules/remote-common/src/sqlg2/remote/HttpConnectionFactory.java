package sqlg2.remote;

import sqlg2.db.IConnectionFactory;
import sqlg2.db.IRemoteDBInterface;
import sqlg2.db.RemoteException;
import sqlg2.db.UnrecoverableRemoteException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.sql.SQLException;

public final class HttpConnectionFactory extends HttpProxy implements IConnectionFactory {

    public HttpConnectionFactory(String url, Proxy proxy, String application) throws URISyntaxException, MalformedURLException {
        this(new URI(url).normalize().toURL(), proxy, application);
    }

    public HttpConnectionFactory(URL url, Proxy proxy, String application) {
        super(new HttpId(application), new Endpoint(url, proxy));
    }

    public IRemoteDBInterface openConnection(String user, String password) throws SQLException {
        try {
            HttpDBInterface db = (HttpDBInterface) httpInvoke(HttpCommand.OPEN, user, password);
            db.setEndpoint(this);
            return db;
        } catch (SQLException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(getEndpoint());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        Endpoint endpoint = (Endpoint) in.readObject();
        setEndpoint(endpoint);
    }

    public URL getUrl() {
        return getEndpoint().getUrl();
    }
}
