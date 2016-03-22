package runtime.db;

import runtime.mapper.RoleId;
import runtime.mapper.UserId;
import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import static runtime.db.ConfigDB.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface IConfigDB extends IDBCommon {

    public List<Row> listRows() throws SQLException;

    public void insertRow(UserId userId, RoleId roleId) throws SQLException;
}
