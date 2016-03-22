package rs.dao;

import sqlg2.*;
import sqlg2.db.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import static rs.dao.OutRS.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface IOutRS extends IDBCommon {

    public List<EmpRow> selectAll() throws SQLException;
}
