package remote.dao;

import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@SQLG
public final class RemoteDAO extends GBase {

    public RemoteDAO(LocalWrapperBase lwb) {
        super(lwb);
    }

    @Business
    public int testMethod() throws SQLException {
        /**
         * SELECT COUNT(*)
         *   FROM EMP
         */
        @Prepare PreparedStatement stmt = prepareStatement("SELECT COUNT(*)\nFROM EMP");
        return singleRowQueryReturningInt(stmt);
    }
}
