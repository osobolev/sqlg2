package big.dao;

import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("UnnecessaryFullyQualifiedName")
@SQLG
public final class BigDAO extends GBase {

    public BigDAO(LocalWrapperBase lwb) {
        super(lwb);
    }

    private static final String FIELDS = "e_main.emp_no, e_main.emp_name, e_main.job, e_main.mgr, e_mgr.emp_name mgr_Name, e_main.hire_date";

    @RowType
    public abstract static class EmpMetaRow {
    }

    /**
     * Get metadata
     */
    @Business
    public EmpMetaRow getEmpMeta() throws SQLException {
        /**
         * SELECT &FIELDS
         *   FROM emp e_main LEFT JOIN emp e_mgr 
         *        ON e_main.mgr = e_mgr.emp_no
         */
        @Prepare PreparedStatement stmt = null;
        return metaRowQuery(stmt, EmpMetaRow.class);
    }

    @EditableRowType
    public abstract static class EmpRow {
    }

    /**
     * Selecting all employees
     */
    @Business
    public List<EmpRow> selectAll() throws SQLException {
        /**
         * SELECT &FIELDS
         *   FROM emp e_main LEFT JOIN emp e_mgr 
         *        ON e_main.mgr = e_mgr.emp_no
         * ORDER BY 1
         */
        @Prepare PreparedStatement stmt = null;
        return multiRowQuery(stmt, EmpRow.class);
    }

    @RowType
    public abstract static class MgrRow {
    }

    /**
     * Selecting all employees as managers
     */
    @Business
    public List<MgrRow> selectAllManagers() throws SQLException {
        /**
         * SELECT emp_no, emp_name
         *   FROM emp
         * ORDER BY 1
         */
        @Prepare PreparedStatement stmt = null;
        return multiRowQuery(stmt, MgrRow.class);
    }

    /**
     * Selecting employees who can be managers for given employee
     */
    @Business
    public List<MgrRow> selectPossibleManagers(int empNo) throws SQLException {
        /**
         * SELECT emp_no, emp_name
         *   FROM emp
         *  WHERE emp_no <> :empNo
         *    AND hire_date < (SELECT hire_date FROM emp WHERE emp_no = :empNo)
         * ORDER BY 1
         */
        @Prepare PreparedStatement stmt = null;
        return multiRowQuery(stmt, MgrRow.class);
    }

    /**
     * Selecting one employee
     */
    private EmpRow select(int empNo) throws SQLException {
        /**
         * SELECT &FIELDS
         *   FROM emp e_main LEFT JOIN emp e_mgr 
         *        ON e_main.mgr = e_mgr.emp_no
         *  WHERE e_main.emp_no = :empNo
         */
        @Prepare PreparedStatement stmt = null;
        return singleRowQuery(stmt, EmpRow.class);
    }

    /**
     * Creating employee
     */
    @Business
    public EmpRow insert(String empName, String job, Integer manager) throws SQLException {
        /**
         * INSERT INTO emp
         *   (emp_name, job, mgr, hire_date)
         *   VALUES
         *   (:empName, :job, :manager, CURRENT_DATE)
         */
        @PrepareKey("emp_no") PreparedStatement stmt = null;
        executeUpdate(stmt);
        int empNo = getGeneratedKeys(stmt)[0].intValue();
        return select(empNo);
    }

    /**
     * Removing employee
     */
    @Business
    public void delete(int empNo) throws SQLException {
        /**
         * DELETE FROM emp
         *  WHERE emp_no = :empNo
         */
        @Prepare PreparedStatement stmt = null;
        executeUpdate(stmt);
    }

    /**
     * Updating employee
     */
    @Business
    public EmpRow update(int empNo, String empName, String job, Integer manager) throws SQLException {
        /**
         * UPDATE emp
         *    SET emp_name = :empName, job = :job, mgr = :manager
         *  WHERE emp_no = :empNo
         */
        @Prepare PreparedStatement stmt = null;
        executeUpdate(stmt);
        return select(empNo);
    }
}
