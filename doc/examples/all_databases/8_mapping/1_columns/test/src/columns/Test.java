package columns;

import columns.db.IUserDB;
import columns.db.UserDB;
import sqlg2.db.*;

import java.sql.Connection;
import java.util.List;

public final class Test {

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

        IUserDB udb = db.getSimpleTransaction().getInterface(IUserDB.class);
        System.out.println("Custom-mapped columns:");
        List<UserDB.Row> rows = udb.listRows();
        for (UserDB.Row row : rows) {
            System.out.println(row.userData());
        }

        db.close();
    }
}
