package layers.dao;

import sqlg2.BusinessNoSql;
import sqlg2.GBase;
import sqlg2.LocalWrapperBase;
import sqlg2.SQLG;

import java.sql.SQLException;

@SQLG
public final class Layer2 extends GBase {

    public Layer2(LocalWrapperBase lwb) {
        super(lwb);
    }

    @BusinessNoSql
    public void insert(int testValue) throws SQLException {
        ILayer1 i1 = getInterface(ILayer1.class);
        i1.insert(testValue);
        throw new SQLException("Failed; must rollback all changes");
    }
}
