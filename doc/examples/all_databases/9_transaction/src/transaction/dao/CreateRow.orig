package transaction.dao;

import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@SQLG
public final class CreateRow extends GBase {

    public CreateRow(LocalWrapperBase lwb) {
        super(lwb);
    }

    /**
     * Inserting one row
     */
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
