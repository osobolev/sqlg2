package j2ee.dao;

import sqlg2.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import static j2ee.dao.TestDAO.*;

import sqlg2.db.IDBCommon;

// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT
@SuppressWarnings({"UnnecessaryInterfaceModifier", "UnnecessaryFullyQualifiedName", "RedundantSuppression"})
public interface ITestDAO extends IDBCommon {

    public Timestamp getTime() throws SQLException;
}
