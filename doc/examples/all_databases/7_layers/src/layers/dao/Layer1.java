package layers.dao;

import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@SQLG
public final class Layer1 extends GBase {

    public Layer1(LocalWrapperBase lwb) {
        super(lwb);
    }

    @Business
    public void insert(int testValue) throws SQLException {
        /**
         * INSERT INTO intest
         *   (val)
         *   VALUES
         *   (:testValue)
         */
        @Prepare PreparedStatement stmt = prepareStatement("INSERT INTO intest\n(val)\nVALUES\n(?)", inP(testValue, Integer.class));
        executeUpdate(stmt);
    }
}
