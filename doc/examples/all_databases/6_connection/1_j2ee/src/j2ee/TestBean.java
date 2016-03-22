package j2ee;

import j2ee.dao.ITestDAO;

import sqlg2.db.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class TestBean {

    private final IDBInterface db;

    public TestBean() throws Exception {
        InitialContext ctx = new InitialContext();
        Context env = (Context) ctx.lookup("java:/comp/env");
        DataSource ds = (DataSource) env.lookup("jdbc/TestDS");
        SQLGLogger logger = new SQLGLogger.Simple();
        ConnectionManager cman = new DataSourceConnectionManager(ds);
        this.db = new LocalDBInterface(cman, new sqlg2.db.specific.Generic(), logger);
    }

    public String getServerTime() {
        try {
            ITestDAO dbc = db.getSimpleTransaction().getInterface(ITestDAO.class);
            Date time = dbc.getTime();
            return DateFormat.getDateTimeInstance().format(time);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
