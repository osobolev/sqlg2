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

        public abstract int empNo();
        public abstract String empName();
        public abstract String job();
        public abstract Integer mgr();
        public abstract java.sql.Date hireDate();
    }

    @Business
    public List<EmpRow> selectAll() throws SQLException {
        /**
         * SELECT emp_no, emp_name, job, mgr, hire_date
         *   FROM emp
         * ORDER BY emp_no
         */
        @Prepare PreparedStatement stmt = prepareStatement("SELECT emp_no, emp_name, job, mgr, hire_date\nFROM emp\nORDER BY emp_no");
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
        @Prepare PreparedStatement stmt = prepareStatement("INSERT INTO emp\n(emp_no, emp_name, job, mgr, hire_date)\nVALUES\n(?, ?, ?, ?, ?)", inP(empNo, Integer.class), inP(empName, String.class), inP(job, String.class), inP(manager, Integer.class), inP(hireDate, java.sql.Timestamp.class));
        executeUpdate(stmt);
    }
}
