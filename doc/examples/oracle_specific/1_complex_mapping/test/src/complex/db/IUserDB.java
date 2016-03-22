package complex.db;

import complex.mapper.UserObj;
import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import static complex.db.UserDB.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface IUserDB extends IDBCommon {

    public List<UserObj> listObjects() throws SQLException;

    public void addNewObject(UserObj user) throws SQLException;
}
