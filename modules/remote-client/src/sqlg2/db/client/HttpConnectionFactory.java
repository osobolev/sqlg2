package sqlg2.db.client;

import sqlg2.db.IConnectionFactory;
import sqlg2.db.IRemoteDBInterface;
import sqlg2.db.RemoteException;
import sqlg2.db.remote.HttpCommand;
import sqlg2.db.remote.HttpDBInterfaceInfo;
import sqlg2.db.remote.HttpId;

import java.io.IOException;
import java.net.*;
import java.sql.SQLException;

/**
 * {@link IConnectionFactory} implementation for remote calls.
 */
public final class HttpConnectionFactory implements IConnectionFactory {

    private final HttpId id;
    private final HttpRootObject rootObject;

    public HttpConnectionFactory(String url, Proxy proxy, String application) throws URISyntaxException, MalformedURLException {
        this(new URI(url).normalize().toURL(), proxy, application);
    }

    public HttpConnectionFactory(final URL url, final Proxy proxy, String application) {
        this(application, new IHttpClientFactory() {
            public IHttpClient getClient() throws IOException {
                return DefaultHttpClient.create(url, proxy, 3000);
            }
        });
    }

    public HttpConnectionFactory(String application, IHttpClientFactory clientFactory) {
        this.id = new HttpId(application);
        this.rootObject = new HttpRootObject(clientFactory);
    }

    public void setSerializer(IClientSerializer serializer) {
        rootObject.setSerializer(serializer);
    }

    public IRemoteDBInterface openConnection(String user, String password) throws SQLException {
        try {
            HttpDBInterfaceInfo info = rootObject.httpInvoke(HttpDBInterfaceInfo.class, HttpCommand.OPEN, id, user, password);
            return new HttpDBInterface(rootObject, info);
        } catch (SQLException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }
}
