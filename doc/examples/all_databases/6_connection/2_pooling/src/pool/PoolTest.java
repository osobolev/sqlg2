package pool;

import pool.dao.IPoolDAO;
import sqlg2.db.*;

/**
 * Example of connection pooling in SQLG application.
 */
public class PoolTest {

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
        ConnectionManager cman = new PoolingConnectionManager(driver, url, username, password);
        IDBInterface db = new LocalDBInterface(cman, dbclass, logger);
        try {
            ITransaction t1 = db.getTransaction();
            ITransaction t2 = db.getTransaction();
            IPoolDAO i1 = t1.getInterface(IPoolDAO.class);
            IPoolDAO i2 = t2.getInterface(IPoolDAO.class);
            System.out.println("Transaction 1: " + i1.testMethod());
            // without pooling following line would lead to deadlock,
            // because transaction holds connection until committed 
            // or rolled back:
            System.out.println("Transaction 2: " + i2.testMethod());
            t1.commit();
            t2.commit();
        } finally {
            db.close();
        }
    }
}
