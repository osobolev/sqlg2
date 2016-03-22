package rs.dao;

import sqlg2.*;
import sqlg2.db.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@SQLG
public final class OutRS extends GBase {

    public OutRS(LocalWrapperBase lwb) {
        super(lwb);
    }

    @Business
    public List<EmpRow> selectAll() throws SQLException {
        TypedList<EmpRow> list = new TypedList<EmpRow>(EmpRow.class);
        /**
         * { call rs_pack.rs_test(:>list) }
         */
        @Call Object[] ret = executeCall("{ call rs_pack.rs_test(?) }", outP(list));
        return list.list;
    }
}
