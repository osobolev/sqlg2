package j2ee.dao;

import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

@SQLG
public final class TestDAO extends GBase {

    public TestDAO(LocalWrapperBase lwb) {
        super(lwb);
    }

    @Business
    public Timestamp getTime() throws SQLException {
        /**
         * SELECT CURRENT_TIMESTAMP FROM (SELECT COUNT(*) FROM EMP) c
         */
        @Prepare PreparedStatement stmt = prepareStatement("SELECT CURRENT_TIMESTAMP FROM (SELECT COUNT(*) FROM EMP) c");
        return singleRowQueryReturningTimestamp(stmt);
    }
}
