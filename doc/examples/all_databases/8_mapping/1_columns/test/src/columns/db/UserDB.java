package columns.db;

import columns.mapper.User;
import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@SQLG
public final class UserDB extends GBase {

    public UserDB(LocalWrapperBase lwb) {
        super(lwb);
    }

    @RowType
    public abstract static class Row {

        public abstract columns.mapper.User userData();
    }

    @Business
    public List<Row> listRows() throws SQLException {
        /**
         * SELECT user_Data#user{id, name}
         *   FROM users
         * ORDER BY name
         */
        @Prepare PreparedStatement stmt = prepareStatement("SELECT user_Data#user{id, name}\nFROM users\nORDER BY name");
        return multiRowQuery(stmt, Row.class);
    }

    @Business
    public Row getNewUserData(User user) throws SQLException {
        /**
         * SELECT user{id, name}
         *   FROM users
         *  WHERE id = :user
         */
        @Prepare PreparedStatement stmt = prepareStatement("SELECT user{id, name}\nFROM users\nWHERE id = ?", inP(user, columns.mapper.User.class));
        return optionalRowQuery(stmt, Row.class);
    }

    /* PREPROCESSOR GENERATED CODE - DO NOT REMOVE THIS LINE */

    public static final String _RUNTIME_MAPPER = "columns.mapper.CustomRuntimeMapperImpl";

    /* PREPROCESSOR GENERATED CODE - DO NOT REMOVE THIS LINE */
}
