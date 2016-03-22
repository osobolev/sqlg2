package sqlg2;

import sqlg2.db.Impl;
import sqlg2.db.SQLGException;
import sqlg2.db.TypedList;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.*;

/**
 * This class represents query parameter (both IN or OUT). Parameter values are usually obtained by
 * {@link GBase#in} or {@link GBase#out} methods.
 */
public final class Parameter implements Serializable {

    private final Object inputValue;
    private final boolean input;
    /**
     * Array, always not null for OUT parameters
     */
    private final Object outputValue;
    private final boolean output;
    private final boolean resultSet;
    private final Class<Object> cls;
    private final Integer jdbcType;

    @SuppressWarnings("unchecked")
    private Parameter(Object inputValue, boolean input,
                      Object outputValue, boolean output,
                      boolean resultSet, Class<?> cls, Integer jdbcType) {
        this.inputValue = inputValue;
        this.input = input;
        this.outputValue = outputValue;
        this.output = output;
        this.resultSet = resultSet;
        this.cls = (Class<Object>) cls;
        this.jdbcType = jdbcType;
    }

    /**
     * Creates parameter with value and its type. Use this method only if you really need to, {@link GBase#in} is
     * more type safe.
     *
     * @param value parameter value (can be null)
     * @param cls parameter type (should be not null)
     */
    public static Parameter in(Object value, Class<?> cls) {
        return new Parameter(
            value, true,
            null, false,
            false, cls, null
        );
    }

    static Parameter out(Object output) {
        return new Parameter(
            null, false,
            output, true,
            false, output.getClass().getComponentType(), null
        );
    }

    static Parameter outJdbc(int jdbcType) {
        return new Parameter(
            null, false,
            null, true,
            false, null, jdbcType
        );
    }

    static Parameter outResultSet(TypedList<?> typedList) {
        return new Parameter(
            null, false,
            typedList, true,
            true, null, null
        );
    }

    /**
     * Creates IN/OUT parameter. Should be an array with at least one element. Its input value is array[0]
     * and output value is stored in array[0] as well.
     *
     * @param array an array with at least one element
     */
    public static Parameter inOut(Object array) {
        return new Parameter(
            Array.get(array, 0), true,
            array, true,
            false, array.getClass().getComponentType(), null
        );
    }

    public String toString() {
        if (inputValue != null) {
            return inputValue.toString();
        } else if (cls != null) {
            return "type " + cls;
        } else if (jdbcType != null) {
            return "type " + jdbcType;
        } else {
            return "?";
        }
    }

    private void doSet(GBase base, PreparedStatement stmt, int index) throws SQLException {
        base.getMapper(cls).set(stmt, index, inputValue, cls);
    }

    void set(GBase base, PreparedStatement stmt, int index) throws SQLException {
        if (!input)
            return;
        if (inputValue == null) {
            Integer type = getStandardParameterType(cls);
            if (type != null) {
                stmt.setNull(index, type.intValue());
            } else {
                doSet(base, stmt, index);
            }
            return;
        }
        if (Byte.class.isAssignableFrom(cls) || Byte.TYPE.isAssignableFrom(cls)) {
            stmt.setByte(index, ((Byte) inputValue).byteValue());
        } else if (Short.class.isAssignableFrom(cls) || Short.TYPE.isAssignableFrom(cls)) {
            stmt.setShort(index, ((Short) inputValue).shortValue());
        } else if (Integer.class.isAssignableFrom(cls) || Integer.TYPE.isAssignableFrom(cls)) {
            stmt.setInt(index, ((Integer) inputValue).intValue());
        } else if (Long.class.isAssignableFrom(cls) || Long.TYPE.isAssignableFrom(cls)) {
            stmt.setLong(index, ((Long) inputValue).longValue());
        } else if (Float.class.isAssignableFrom(cls) || Float.TYPE.isAssignableFrom(cls)) {
            stmt.setFloat(index, ((Float) inputValue).floatValue());
        } else if (Double.class.isAssignableFrom(cls) || Double.TYPE.isAssignableFrom(cls)) {
            stmt.setDouble(index, ((Double) inputValue).doubleValue());
        } else if (BigDecimal.class.isAssignableFrom(cls)) {
            stmt.setBigDecimal(index, (BigDecimal) inputValue);
        } else if (String.class.isAssignableFrom(cls)) {
            stmt.setString(index, (String) inputValue);
        } else if (Boolean.class.isAssignableFrom(cls) || Boolean.TYPE.isAssignableFrom(cls)) {
            stmt.setBoolean(index, ((Boolean) inputValue).booleanValue());
        } else if (Parameter.class.isAssignableFrom(cls)) {
            ((Parameter) inputValue).set(base, stmt, index);
        } else if (java.util.Date.class.isAssignableFrom(cls)) {
            long time = ((java.util.Date) inputValue).getTime();
            if (Date.class.isAssignableFrom(cls)) {
                stmt.setDate(index, new Date(time));
            } else if (Time.class.isAssignableFrom(cls)) {
                stmt.setTime(index, new Time(time));
            } else {
                stmt.setTimestamp(index, new Timestamp(time));
            }
        } else if (byte[].class.isAssignableFrom(cls)) {
            stmt.setBytes(index, (byte[]) inputValue);
        } else {
            doSet(base, stmt, index);
        }
    }

    private static Integer getStandardParameterType(Class<?> cls) {
        if (Byte.class.isAssignableFrom(cls) || Byte.TYPE.isAssignableFrom(cls)) {
            return Types.TINYINT;
        } else if (Short.class.isAssignableFrom(cls) || Short.TYPE.isAssignableFrom(cls)) {
            return Types.SMALLINT;
        } else if (Integer.class.isAssignableFrom(cls) || Integer.TYPE.isAssignableFrom(cls)) {
            return Types.INTEGER;
        } else if (Long.class.isAssignableFrom(cls) || Long.TYPE.isAssignableFrom(cls)) {
            return Types.BIGINT;
        } else if (Float.class.isAssignableFrom(cls) || Float.TYPE.isAssignableFrom(cls)) {
            return Types.FLOAT;
        } else if (Double.class.isAssignableFrom(cls) || Double.TYPE.isAssignableFrom(cls)) {
            return Types.DOUBLE;
        } else if (BigDecimal.class.isAssignableFrom(cls)) {
            return Types.NUMERIC;
        } else if (String.class.isAssignableFrom(cls)) {
            return Types.VARCHAR;
        } else if (Boolean.class.isAssignableFrom(cls) || Boolean.TYPE.isAssignableFrom(cls)) {
            return Types.BOOLEAN;
        } else if (java.util.Date.class.isAssignableFrom(cls)) {
            if (Date.class.isAssignableFrom(cls)) {
                return Types.DATE;
            } else if (Time.class.isAssignableFrom(cls)) {
                return Types.TIME;
            } else {
                return Types.TIMESTAMP;
            }
        } else if (byte[].class.isAssignableFrom(cls)) {
            return Types.VARBINARY;
        } else {
            return null;
        }
    }

    private void register(GBase base, CallableStatement cs, int index) throws SQLException {
        if (resultSet) {
            cs.registerOutParameter(index, base.lwb.getSpecific().getResultSetType());
            return;
        }
        if (jdbcType != null) {
            cs.registerOutParameter(index, jdbcType.intValue());
            return;
        }
        Integer type = getStandardParameterType(cls);
        if (type != null) {
            cs.registerOutParameter(index, type.intValue());
        } else {
            base.getMapper(cls).register(cs, index, cls);
        }
    }

    private Object doGet(GBase base, CallableStatement cs, int index) throws SQLException {
        if (jdbcType != null) {
            switch (jdbcType.intValue()) {
            case Types.TINYINT:
                return cs.getByte(index);
            case Types.SMALLINT:
                return cs.getShort(index);
            case Types.INTEGER:
                return cs.getInt(index);
            case Types.BIGINT:
                return cs.getLong(index);
            case Types.FLOAT:
                return cs.getFloat(index);
            case Types.REAL:
            case Types.DOUBLE:
                return cs.getDouble(index);
            case Types.DECIMAL:
            case Types.NUMERIC:
                return cs.getBigDecimal(index);
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
                return cs.getString(index);
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return cs.getBytes(index);
            case Types.BOOLEAN:
                return cs.getBoolean(index);
            case Types.DATE:
                return cs.getDate(index);
            case Types.TIME:
                return cs.getTime(index);
            case Types.TIMESTAMP:
                return cs.getTimestamp(index);
            case Types.CLOB:
                Clob clob = cs.getClob(index);
                String str = Impl.getCLOB(clob);
                if (clob != null) {
                    try {
                        base.lwb.getSpecific().freeClob(base.lwb.getConnection(), clob);
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
                return str;
            case Types.BLOB:
                Blob blob = cs.getBlob(index);
                byte[] res = Impl.getBLOB(blob);
                if (blob != null) {
                    try {
                        base.lwb.getSpecific().freeBlob(base.lwb.getConnection(), blob);
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
                return res;
            default:
                return cs.getObject(index);
            }
        }
        if (Byte.class.isAssignableFrom(cls) || Byte.TYPE.isAssignableFrom(cls)) {
            return cs.getByte(index);
        } else if (Short.class.isAssignableFrom(cls) || Short.TYPE.isAssignableFrom(cls)) {
            return cs.getShort(index);
        } else if (Integer.class.isAssignableFrom(cls) || Integer.TYPE.isAssignableFrom(cls)) {
            return cs.getInt(index);
        } else if (Long.class.isAssignableFrom(cls) || Long.TYPE.isAssignableFrom(cls)) {
            return cs.getLong(index);
        } else if (Float.class.isAssignableFrom(cls) || Float.TYPE.isAssignableFrom(cls)) {
            return cs.getFloat(index);
        } else if (Double.class.isAssignableFrom(cls) || Double.TYPE.isAssignableFrom(cls)) {
            return cs.getDouble(index);
        } else if (BigDecimal.class.isAssignableFrom(cls)) {
            return cs.getBigDecimal(index);
        } else if (String.class.isAssignableFrom(cls)) {
            return cs.getString(index);
        } else if (Boolean.class.isAssignableFrom(cls) || Boolean.TYPE.isAssignableFrom(cls)) {
            return cs.getBoolean(index);
        } else if (java.util.Date.class.isAssignableFrom(cls)) {
            if (Date.class.isAssignableFrom(cls)) {
                return cs.getDate(index);
            } else if (Time.class.isAssignableFrom(cls)) {
                return cs.getTime(index);
            } else {
                return cs.getTimestamp(index);
            }
        } else if (byte[].class.isAssignableFrom(cls)) {
            return cs.getBytes(index);
        } else {
            return base.getMapper(cls).get(cs, index, cls);
        }
    }

    Object get(GBase base, CallableStatement cs, int index) throws SQLException {
        if (GBase.test != null) {
            return GBase.test.getValue(cls, jdbcType);
        }
        Object res = doGet(base, cs, index);
        if (outputValue != null) {
            Array.set(outputValue, 0, res);
        }
        return res;
    }

    public boolean isOut() {
        return output;
    }

    TypedList<?> getResultSet() {
        if (resultSet) {
            return (TypedList<?>) outputValue;
        } else {
            return null;
        }
    }

    /**
     * Binds {@link PreparedStatement} parameters to values from array.
     * Can be used for {@link CallableStatement}, in this case it is possible
     * to use {@link Parameter} in array.
     *
     * @param in values array
     */
    static void setParameters(GBase base, PreparedStatement st, Parameter[] in) throws SQLException {
        if (in == null)
            return;
        for (int i = 0; i < in.length; i++) {
            Parameter param = in[i];
            int j = i + 1;
            if (param.isOut()) {
                if (st instanceof CallableStatement) {
                    param.set(base, st, j);
                    param.register(base, (CallableStatement) st, j);
                } else {
                    throw new SQLGException("You can pass OUT parameter only to block");
                }
            } else {
                param.set(base, st, j);
            }
        }
    }
}
