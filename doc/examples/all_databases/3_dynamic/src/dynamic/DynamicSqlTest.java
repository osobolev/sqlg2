package dynamic;

import dynamic.dao.DynamicSql;
import dynamic.dao.IDynamicSql;
import sqlg2.db.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Example of SQLG-generated dynamic SQL code usage.
 */
public class DynamicSqlTest {

    public static void print(IDynamicSql iex1) throws SQLException {
        // Calling business method
        List<DynamicSql.EmpRow> list = iex1.selectFilter1("KIN*", null, null, null);
        // Printing result
        for (DynamicSql.EmpRow emp : list) {
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
            // For Oracle database should be sqlg2.db.specific.Oracle
            dbclass = (DBSpecific) Class.forName(args[4]).newInstance();
        }
        SQLGLogger logger = new SQLGLogger.Simple();
        // Opening connection
        Connection connection = SingleConnectionManager.openConnection(driver, url, username, password);
        ConnectionManager cman = new SingleConnectionManager(connection);
        IDBInterface db = new LocalDBInterface(cman, dbclass, logger);
        try {
            // Getting data access interface
            IDynamicSql iex1 = db.getSimpleTransaction().getInterface(IDynamicSql.class);
            // Running business methods
            print(iex1);
        } finally {
            db.close(); // closing DB connection
        }
    }
}
