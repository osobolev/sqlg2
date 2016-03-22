package complex.db;

import complex.mapper.UserObj;
import sqlg2.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@SQLG
public final class UserDB extends GBase {

    public UserDB(LocalWrapperBase lwb) {
        super(lwb);
    }

    @Business
    public List<UserObj> listObjects() throws SQLException {
        /**
         * SELECT userobj{userobj}
         *   FROM objusers
         */
        @Prepare PreparedStatement stmt = prepareStatement("SELECT userobj{userobj}\nFROM objusers");
        return columnOf(UserObj.class, stmt);
    }

    @Business
    public void addNewObject(UserObj user) throws SQLException {
        /**
         * INSERT INTO objusers (userobj)
         * VALUES (:user)
         */
        @Prepare PreparedStatement stmt = prepareStatement("INSERT INTO objusers (userobj)\nVALUES (?)", inP(user, complex.mapper.UserObj.class));
        executeUpdate(stmt);
    }

    /* PREPROCESSOR GENERATED CODE - DO NOT REMOVE THIS LINE */

    public static final String _RUNTIME_MAPPER = "complex.mapper.CustomRuntimeMapperImpl";

    /* PREPROCESSOR GENERATED CODE - DO NOT REMOVE THIS LINE */
}
