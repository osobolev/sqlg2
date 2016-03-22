package dynamic.dao;

import sqlg2.*;
import sqlg2.db.QueryBuilder;
import sqlg2.db.QueryPiece;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@SQLG
public final class DynamicSql extends GBase {

    public DynamicSql(LocalWrapperBase lwb) {
        super(lwb);
    }

    @RowType
    public abstract static class EmpRow {

        public abstract int empNo();
        public abstract String empName();
        public abstract String job();
        public abstract Integer mgr();
        public abstract java.sql.Date hireDate();
    }

    /**
     * Using query pieces. The advantage of this approach that it can build really complex queries,
     * some parts of which can come as parameters, so you can extract common query building code into single method
     * and call it with different pieces to modify necessary query parts.
     */
    private List<EmpRow> selectEmps(QueryPiece where) throws SQLException {
        /**
         * SELECT emp_no, emp_name, job, mgr, hire_date
         *   FROM emp
         *  WHERE &where
         * ORDER BY emp_no
         */
        @Prepare PreparedStatement stmt = prepareStatement(new sqlg2.db.QueryBuilder("SELECT emp_no, emp_name, job, mgr, hire_date\nFROM emp\nWHERE ").appendLit(where).appendLit("\nORDER BY emp_no").toQuery());
        return multiRowQuery(stmt, EmpRow.class);
    }

    /**
     * Building filter with query pieces.
     */
    @Business
    public List<EmpRow> selectFilter1(String name, String job, Timestamp dateFrom, Timestamp dateTo) throws SQLException {
        QueryBuilder buf = new QueryBuilder("1=1");
        if (name != null) {
            String mask = name.replace('*', '%').replace('?', '_');
            /** AND emp_name LIKE :mask */
            @Query QueryPiece byName = createQueryPiece("AND emp_name LIKE ?", inP(mask, String.class));
            buf.append(byName);
        }
        if (job != null) {
            /** AND job LIKE :job */
            @Query QueryPiece byJob = createQueryPiece("AND job LIKE ?", inP(job, String.class));
            buf.append(byJob);
        }
        if (dateFrom != null) {
            /** AND hire_date >= :dateFrom */
            @Query QueryPiece byDateFrom = createQueryPiece("AND hire_date >= ?", inP(dateFrom, java.sql.Timestamp.class));
            buf.append(byDateFrom);
        }
        if (dateTo != null) {
            /** AND hire_date <= :dateTo */
            @Query QueryPiece byDateTo = createQueryPiece("AND hire_date <= ?", inP(dateTo, java.sql.Timestamp.class));
            buf.append(byDateTo);
        }
        QueryPiece where = buf.toQuery();
        return selectEmps(where);
    }

    /**
     * Reusing query with different subquery.
     */
    @Business
    public List<EmpRow> selectById(int id) throws SQLException {
        /** emp_no = :id */
        @Query QueryPiece where = createQueryPiece("emp_no = ?", inP(id, Integer.class));
        return selectEmps(where);
    }

    /**
     * Building filter with strings. For simple queries it is a more simple way.
     */
    @Business
    public List<EmpRow> selectFilter2(String name, String job, Timestamp dateFrom, Timestamp dateTo) throws SQLException {
        /**
         * SELECT emp_no, emp_name, job, mgr, hire_date
         *   FROM emp
         *  WHERE 1 = 1
         */
        @Sql String sql1 = "SELECT emp_no, emp_name, job, mgr, hire_date\nFROM emp\nWHERE 1 = 1";
        StringBuilder buf = new StringBuilder(sql1);
        if (name != null) {
            buf.append(" AND emp_name LIKE ?");
        }
        if (job != null) {
            buf.append(" AND job LIKE ?");
        }
        if (dateFrom != null) {
            buf.append(" AND hire_date >= ?");
        }
        if (dateTo != null) {
            buf.append(" AND hire_date <= ?");
        }
        buf.append(" ORDER BY emp_no");
        PreparedStatement stmt = prepareStatement(buf.toString());
        int index = 1;
        if (name != null) {
            String mask = name.replace('*', '%').replace('?', '_');
            stmt.setString(index++, mask);
        }
        if (job != null) {
            stmt.setString(index++, job);
        }
        if (dateFrom != null) {
            stmt.setTimestamp(index++, dateFrom);
        }
        if (dateTo != null) {
            stmt.setTimestamp(index++, dateTo);
        }
        return multiRowQuery(stmt, EmpRow.class);
    }
}
