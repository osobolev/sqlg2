package dynamic.dao;

import sqlg2.*;
import sqlg2.db.QueryBuilder;
import sqlg2.db.QueryPiece;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import static dynamic.dao.DynamicSql.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface IDynamicSql extends IDBCommon {

    /**
     * Building filter with query pieces.
     */
    public List<EmpRow> selectFilter1(String name, String job, Timestamp dateFrom, Timestamp dateTo) throws SQLException;

    /**
     * Reusing query with different subquery.
     */
    public List<EmpRow> selectById(int id) throws SQLException;

    /**
     * Building filter with strings. For simple queries it is a more simple way.
     */
    public List<EmpRow> selectFilter2(String name, String job, Timestamp dateFrom, Timestamp dateTo) throws SQLException;
}
