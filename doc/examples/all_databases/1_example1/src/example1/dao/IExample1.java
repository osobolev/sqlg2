package example1.dao;

import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import static example1.dao.Example1.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface IExample1 extends IDBCommon {

    public List<EmpRow> selectAll() throws SQLException;

    public void insert(int empNo, String empName, String job,
                       Integer manager, Timestamp hireDate) throws SQLException;
}
