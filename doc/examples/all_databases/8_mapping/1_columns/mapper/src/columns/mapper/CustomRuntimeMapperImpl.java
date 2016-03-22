package columns.mapper;

import sqlg2.db.CustomMapper;
import sqlg2.db.RuntimeMapperImpl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class CustomRuntimeMapperImpl extends RuntimeMapperImpl {

    public <T> CustomMapper<T> getMapper(Class<T> cls) {
        if (User.class.isAssignableFrom(cls)) {
            return cast(new CustomMapper<User>() {

                public User fetch(ResultSet rs, int from, int to, Class<User> cls) throws SQLException {
                    long id = rs.getLong(from);
                    String name = rs.getString(from + 1);
                    return new User(id, name);
                }

                public void set(PreparedStatement stmt, int index, User value, Class<User> cls) throws SQLException {
                    stmt.setLong(index, value.id);
                }
            });
        } else {
            return super.getMapper(cls);
        }
    }
}
