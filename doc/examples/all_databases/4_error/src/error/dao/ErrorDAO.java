package error.dao;

import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

/**
 * Example of errors in SQL; they all can be caught during preprocess phase
 */
@SQLG
public final class ErrorDAO extends GBase {

    public ErrorDAO(LocalWrapperBase lwb) {
        super(lwb);
    }

    @RowType
    public abstract static class EmpRow {
    }

    @Business
    public List<EmpRow> selectAll() throws SQLException {
        // todo 1: should be EMP_NO, not EMPNO
        /**
         * SELECT EMPNO, EMP_NAME, JOB, MGR, HIRE_DATE
         *   FROM EMP
         * ORDER BY EMP_NO
         */
        @Prepare PreparedStatement stmt = null;
        return multiRowQuery(stmt, EmpRow.class);
    }

    @Business
    public int insert(String empName, String job,
                      Integer manager, Timestamp hireDate) throws SQLException {
        // todo 2: should have five parameters, not four - add :hireDate
        /**
         * INSERT INTO EMP
         *  (EMP_NAME, JOB, MGR, HIRE_DATE)
         *  VALUES
         *  (:empName, :job, :manager)
         */
        @PrepareKey("EMP_NO") PreparedStatement stmt = null;
        executeUpdate(stmt);
        return getGeneratedKeys(stmt)[0].intValue();
    }
}
