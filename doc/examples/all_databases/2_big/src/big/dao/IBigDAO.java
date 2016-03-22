package big.dao;

import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import static big.dao.BigDAO.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface IBigDAO extends IDBCommon {

    /**
     * Get metadata
     */
    public EmpMetaRow getEmpMeta() throws SQLException;

    /**
     * Selecting all employees
     */
    public List<EmpRow> selectAll() throws SQLException;

    /**
     * Selecting all employees as managers
     */
    public List<MgrRow> selectAllManagers() throws SQLException;

    /**
     * Selecting employees who can be managers for given employee
     */
    public List<MgrRow> selectPossibleManagers(int empNo) throws SQLException;

    /**
     * Creating employee
     */
    public EmpRow insert(String empName, String job, Integer manager) throws SQLException;

    /**
     * Removing employee
     */
    public void delete(int empNo) throws SQLException;

    /**
     * Updating employee
     */
    public EmpRow update(int empNo, String empName, String job, Integer manager) throws SQLException;
}
