package remote;

import remote.dao.IRemoteDAO;
import sqlg2.db.*;
import sqlg2.db.client.*;

import java.net.Proxy;

/**
 * Example of remote data access client.
 */
public final class Client {

    public static void runClient(String username, String password) throws Exception {
        // Getting root object from Naming
        IConnectionFactory conf = new HttpConnectionFactory("http://localhost:" + Server.HTTP_PORT + "/" + Server.HTTP_NAME, Proxy.NO_PROXY, Server.HTTP_NAME);
        // Logging in...
        IRemoteDBInterface db = conf.openConnection(username, password);
        // Wrapping in SafeDBInterface - does useful caching and gives server activity status
        SQLGLogger logger = new SQLGLogger.Simple();
        db = new SafeDBInterface(logger, db);
        // Getting data access interface
        IRemoteDAO dao = db.getSimpleTransaction().getInterface(IRemoteDAO.class);
        // Calling business method
        int result = dao.testMethod();
        System.out.println("Server returned: " + result);
        // Closing connection
        db.close();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Not all DB properties specified");
        } else {
            runClient(args[0], args[1]);
        }
    }
}
