package remote;

import java.sql.Connection;
import java.sql.SQLException;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import sqlg2.db.*;

/**
 * Example of remote data access server.
 * This class need not be changed on adding more DAO classes.
 * The only requirement is that these DAO classes could be loaded with current classloader,
 * since their loading is done dynamically by the request from client.
 */
public final class Server {

    public static final String HTTP_NAME = "sqlg_remote";
    public static final int HTTP_PORT = 8088;

    public static void runServer(final String driver, final String url, String db) throws Exception {
        DBSpecific dbclass = (DBSpecific) Class.forName(db).newInstance();

        // Starting HTTP server
        System.out.println("Running HTTP on port " + HTTP_PORT);
        
        SQLGLogger logger = new SQLGLogger.Simple();
        SessionFactory sessionFactory = new SessionFactory() {
            public SessionData login(SQLGLogger logger, String user, String password) throws SQLException {
                Connection connection = SingleConnectionManager.openConnection(driver, url, user, password);
                ConnectionManager cman = new SingleConnectionManager(connection);
                return new SessionData(cman, user);
            }
        };
        HttpDispatcher http = new HttpDispatcher(HTTP_NAME, sessionFactory, dbclass, logger);

        org.eclipse.jetty.server.Server jetty = new org.eclipse.jetty.server.Server(HTTP_PORT);
        ServletContextHandler ctx = new ServletContextHandler(jetty, "/", ServletContextHandler.NO_SESSIONS);
        SqlgServlet appServlet = new SqlgServlet(http);
        ServletHolder servletHolder = new ServletHolder(appServlet);
        servletHolder.setName(HTTP_NAME);
        ctx.addServlet(servletHolder, "/" + HTTP_NAME + "/*");
        jetty.start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Not all DB properties specified");
        } else {
            runServer(args[0], args[1], args[2]);
        }
    }
}
