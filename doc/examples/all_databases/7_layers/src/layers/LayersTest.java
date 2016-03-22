package layers;

import layers.dao.ILayer2;
import sqlg2.db.*;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple example of SQLG-generated code usage.
 */
public class LayersTest {

    public static void testInsert(ILayer2 i2) throws SQLException {
        // Calling business method
        i2.insert(33);
    }

    public static void main(String[] args) throws Exception {
        // Database properties to use
        String driver, url, username, password;
        DBSpecific dbclass;
        if (args.length < 5) {
            System.err.println("Not all DB properties specified");
            return;
        } else {
            // For Oracle database should be oracle.jdbc.driver.OracleDriver
            driver = args[0];
            url = args[1];
            username = args[2];
            password = args[3];
            // For Oracle database should be sqlg2.db.specific.Oracle
            dbclass = (DBSpecific) Class.forName(args[4]).newInstance();
        }
        SQLGLogger logger = new SQLGLogger.Simple();
        // Opening connection
        Connection connection = SingleConnectionManager.openConnection(driver, url, username, password);
        ConnectionManager cman = new SingleConnectionManager(connection);
        IDBInterface db = new LocalDBInterface(cman, dbclass, logger);
        // All the code above was a preparation required to setup database
        // connection, now real work begins:
        try {
            // Getting data access interface
            ILayer2 i2 = db.getSimpleTransaction().getInterface(ILayer2.class);
            // Running business methods
            testInsert(i2);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            db.close(); // closing DB connection
        }
    }
}
