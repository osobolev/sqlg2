package example1;

import example1.dao.Example1;
import example1.dao.IExample1;
import sqlg2.db.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * Simple example of SQLG-generated code usage.
 */
public class Example1Test {

    public static void insertScott(IExample1 iex1) throws SQLException {
        // Calling business method
        iex1.insert(7788, "SCOTT", "ANALYST", null, new Timestamp(System.currentTimeMillis()));
    }

    public static void print(IExample1 iex1) throws SQLException {
        // Calling business method
        List<Example1.EmpRow> list = iex1.selectAll();
        // Printing result
        for (Example1.EmpRow emp : list) {
            System.out.println(emp.empNo() + "\t" + emp.empName());
        }
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
            // For Oracle database should be sqlg2.db.specific.OracleDB
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
            IExample1 iex1 = db.getSimpleTransaction().getInterface(IExample1.class);
            // Running business methods
            insertScott(iex1);
            print(iex1);
        } finally {
            db.close(); // closing DB connection
        }
    }
}
