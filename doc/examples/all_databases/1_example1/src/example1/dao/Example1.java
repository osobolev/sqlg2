package example1.dao;

import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@SQLG
public final class Example1 extends GBase {

    public Example1(LocalWrapperBase lwb) {
        super(lwb);
    }

    @RowType
    public abstract static class EmpRow {
    }

    @Business
    public List<EmpRow> selectAll() throws SQLException {
        /**
         * SELECT emp_no, emp_name, job, mgr, hire_date
         *   FROM emp
         * ORDER BY emp_no
         */
        @Prepare PreparedStatement stmt = null;
        return multiRowQuery(stmt, EmpRow.class);
    }

    @Business
    public void insert(int empNo, String empName, String job,
                       Integer manager, Timestamp hireDate) throws SQLException {
        /**
         * INSERT INTO emp
         *   (emp_no, emp_name, job, mgr, hire_date)
         *   VALUES
         *   (:empNo, :empName, :job, :manager, :hireDate)
         */
        @Prepare PreparedStatement stmt = null;
        executeUpdate(stmt);
    }
}
