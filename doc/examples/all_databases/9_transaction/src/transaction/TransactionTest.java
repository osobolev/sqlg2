package transaction;

import transaction.dao.CreateRow;
import transaction.dao.ICreateRow;
import sqlg2.db.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class TransactionTest {

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
            // Running two statements in the same transaction:
            TransactionRunnable.runInTransaction(
                logger, db, new TransactionRunnable<Void>() {
                    protected Void run() throws SQLException {
                        ICreateRow iex1 = getInterface(ICreateRow.class);
                        Timestamp date = new Timestamp(System.currentTimeMillis());
                        iex1.insert(7788, "SCOTT", "ANALYST", null, date);
                        iex1.insert(7789, "KING", "MANAGER", null, date);
                        // Explicit commit - one of commit or rollback is required,
                        // else transaction is rolled back and exception is thrown
                        commit();
                        return null;
                    }
                }
            );
        } finally {
            db.close(); // closing DB connection
        }
    }
}
