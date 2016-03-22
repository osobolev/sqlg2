package complex.mapper;

import oracle.sql.STRUCT;
import oracle.sql.StructDescriptor;
import sqlg2.db.CustomMapper;
import sqlg2.db.RuntimeMapperImpl;

import java.sql.*;

public final class CustomRuntimeMapperImpl extends RuntimeMapperImpl {

    private static StructDescriptor descriptor = null;

    static StructDescriptor getDescriptor(Connection conn) throws SQLException {
        synchronized (UserObj.class) {
            if (descriptor == null) {
                String schema = conn.getMetaData().getUserName();
                descriptor = StructDescriptor.createDescriptor(schema + ".T_USEROBJ", conn);
            }
            return descriptor;
        }
    }

    public <T> CustomMapper<T> getMapper(Class<T> cls) {
        if (UserObj.class.isAssignableFrom(cls)) {
            return cast(new CustomMapper<UserObj>() {

                public UserObj fetch(ResultSet rs, int from, int to, Class<UserObj> cls) throws SQLException {
                    Struct user = (Struct) rs.getObject(from);
                    Object[] attrs = user.getAttributes();
                    long id = ((Number) attrs[0]).longValue();
                    String name = (String) attrs[1];
                    return new UserObj(id, name);
                }

                public void set(PreparedStatement stmt, int index, UserObj value, Class<UserObj> cls) throws SQLException {
                    Connection conn = stmt.getConnection();
                    StructDescriptor descriptor = getDescriptor(conn);
                    if (value == null) {
                        stmt.setNull(index, descriptor.getTypeCode(), descriptor.getTypeName());
                    } else {
                        stmt.setObject(index, new STRUCT(descriptor, conn, new Object[] {value.id, value.name}));
                    }
                }
            });
        } else {
            return super.getMapper(cls);
        }
    }
}
