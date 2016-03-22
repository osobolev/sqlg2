package runtime.mapper;

import sqlg2.db.CustomMapper;
import sqlg2.db.RuntimeMapperImpl;

import java.sql.*;

public final class CustomRuntimeMapperImpl extends RuntimeMapperImpl {

    private abstract static class IdMapper<T extends AbstractId> extends CustomMapper<T> {

        public T fetch(ResultSet rs, int from, int to, Class<T> cls) throws SQLException {
            long id = rs.getLong(from);
            if (rs.wasNull())
                return null;
            return create(id);
        }

        protected abstract T create(long id);

        public void set(PreparedStatement stmt, int index, T value, Class<T> cls) throws SQLException {
            stmt.setLong(index, value.getId());
        }

        public void register(CallableStatement cs, int index, Class<T> cls) throws SQLException {
            cs.registerOutParameter(index, Types.BIGINT);
        }

        public T get(CallableStatement cs, int index, Class<T> cls) throws SQLException {
            long id = cs.getLong(index);
            if (cs.wasNull())
                return null;
            return create(id);
        }
    }

    public <T> CustomMapper<T> getMapper(Class<T> cls) {
        if (UserId.class.isAssignableFrom(cls)) {
            return cast(new IdMapper<UserId>() {
                protected UserId create(long id) {
                    return new UserId(id);
                }
            });
        }
        if (RoleId.class.isAssignableFrom(cls)) {
            return cast(new IdMapper<RoleId>() {
                protected RoleId create(long id) {
                    return new RoleId(id);
                }
            });
        }
        return super.getMapper(cls);
    }
}
