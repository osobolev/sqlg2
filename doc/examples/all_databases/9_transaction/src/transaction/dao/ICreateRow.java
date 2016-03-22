package transaction.dao;

import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import static transaction.dao.CreateRow.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface ICreateRow extends IDBCommon {

    /**
     * Inserting one row
     */
    public void insert(int empNo, String empName, String job,
                       Integer manager, Timestamp hireDate) throws SQLException;
}
