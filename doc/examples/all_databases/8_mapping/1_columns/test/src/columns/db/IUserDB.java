package columns.db;

import columns.mapper.User;
import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import static columns.db.UserDB.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface IUserDB extends IDBCommon {

    public List<Row> listRows() throws SQLException;

    public Row getNewUserData(User user) throws SQLException;
}
