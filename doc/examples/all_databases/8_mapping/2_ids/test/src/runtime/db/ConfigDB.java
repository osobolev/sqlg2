package runtime.db;

import runtime.mapper.RoleId;
import runtime.mapper.UserId;
import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("UnnecessaryFullyQualifiedName")
@SQLG
public final class ConfigDB extends GBase {

    public ConfigDB(LocalWrapperBase lwb) {
        super(lwb);
    }

    @RowType
    public abstract static class Row {

        public abstract runtime.mapper.UserId userId();
        public abstract runtime.mapper.RoleId roleId();
    }

    @Business
    public List<Row> listRows() throws SQLException {
        /**
         * SELECT UserId{user_id}, RoleId{role_id}
         *   FROM config
         */
        @Prepare PreparedStatement stmt = prepareStatement("SELECT UserId{user_id}, RoleId{role_id}\nFROM config");
        return multiRowQuery(stmt, Row.class);
    }

    @Business
    public void insertRow(UserId userId, RoleId roleId) throws SQLException {
        /**
         * INSERT INTO config (user_id, role_id) VALUES (:userId, :roleId)
         */
        @Prepare PreparedStatement stmt = prepareStatement("INSERT INTO config (user_id, role_id) VALUES (?, ?)", inP(userId, runtime.mapper.UserId.class), inP(roleId, runtime.mapper.RoleId.class));
        executeUpdate(stmt);
    }

    /* PREPROCESSOR GENERATED CODE - DO NOT REMOVE THIS LINE */

    public static final String _RUNTIME_MAPPER = "runtime.mapper.CustomRuntimeMapperImpl";

    /* PREPROCESSOR GENERATED CODE - DO NOT REMOVE THIS LINE */
}
