package sqlg2;

import sqlg2.db.SQLGException;
import sqlg2.queries.QueryParser;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Default {@link Mapper} implementation
 */
public class MapperImpl implements Mapper {

    private static final String JAVA_LANG_PREFIX = "java.lang";

    public static final class SpecialType {

        public final int paramCount;
        public final Class<?> cls;

        public SpecialType(int paramCount, Class<?> cls) {
            this.paramCount = paramCount;
            this.cls = cls;
        }
    }

    public Object getTestObject(Class<?> paramType) {
        if (paramType.equals(Integer.TYPE) || paramType.equals(Integer.class)) {
            return GTest.INT;
        } else if (paramType.equals(Short.TYPE) || paramType.equals(Short.class)) {
            return GTest.SHORT;
        } else if (paramType.equals(Long.TYPE) || paramType.equals(Long.class)) {
            return GTest.LONG;
        } else if (paramType.equals(Byte.TYPE) || paramType.equals(Byte.class)) {
            return GTest.BYTE;
        } else if (paramType.equals(Float.TYPE) || paramType.equals(Float.class)) {
            return GTest.FLOAT;
        } else if (paramType.equals(Double.TYPE) || paramType.equals(Double.class)) {
            return GTest.DOUBLE;
        } else if (BigDecimal.class.isAssignableFrom(paramType)) {
            return GTest.BIG_DECIMAL;
        } else if (paramType.equals(Character.TYPE) || paramType.equals(Character.class)) {
            return GTest.CHAR;
        } else if (paramType.equals(Boolean.TYPE) || paramType.equals(Boolean.class)) {
            return GTest.BOOLEAN;
        } else if (paramType.equals(String.class)) {
            return GTest.STRING;
        } else if (paramType.equals(StringBuilder.class)) {
            return new StringBuilder(GTest.STRING);
        } else if (paramType.equals(StringBuffer.class)) {
            return new StringBuffer(GTest.STRING);
        } else if (paramType.equals(Parameter.class)) {
            return Parameter.in(GTest.STRING, String.class);
        } else if (java.util.Date.class.isAssignableFrom(paramType)) {
            if (Date.class.isAssignableFrom(paramType)) {
                return GTest.dateValue();
            } else if (Time.class.isAssignableFrom(paramType)) {
                return GTest.timeValue();
            } else if (Timestamp.class.isAssignableFrom(paramType)) {
                return GTest.timestampValue();
            } else {
                return GTest.javaDateValue();
            }
        } else if (paramType.isArray()) {
            Class<?> cls = paramType.getComponentType();
            Object array = Array.newInstance(cls, 1);
            Array.set(array, 0, getTestObject(cls));
            return array;
        } else if (paramType.isInterface()) {
            return Proxy.newProxyInstance(paramType.getClassLoader(), new Class<?>[] {paramType}, (proxy, method, args) -> {
                Class<?> retType = method.getReturnType();
                return getTestObject(retType);
            });
        } else if (Enum.class.isAssignableFrom(paramType)) {
            try {
                Method method = paramType.getMethod("values");
                Object[] values = (Object[]) method.invoke(null);
                return values[0];
            } catch (Exception ex) {
                // ignore
            }
            return null;
        } else {
            try {
                Constructor<?> constructor = paramType.getConstructor();
                return constructor.newInstance();
            } catch (Exception ex) {
                // ignore
            }
            return null;
        }
    }

    public String getClassName(Class<?> cls, String[] packs) {
        int count = 0;
        while (cls.isArray()) {
            cls = cls.getComponentType();
            count++;
        }
        if (count > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append(getClassName(cls, packs));
            for (int i = 0; i < count; i++) {
                buf.append("[]");
            }
            return buf.toString();
        }
        if (cls.equals(Integer.TYPE)) {
            return "int";
        } else if (cls.equals(Short.TYPE)) {
            return "short";
        } else if (cls.equals(Long.TYPE)) {
            return "long";
        } else if (cls.equals(Byte.TYPE)) {
            return "byte";
        } else if (cls.equals(Float.TYPE)) {
            return "float";
        } else if (cls.equals(Double.TYPE)) {
            return "double";
        } else if (cls.equals(Character.TYPE)) {
            return "char";
        } else if (cls.equals(Boolean.TYPE)) {
            return "boolean";
        } else if (cls.equals(Void.TYPE)) {
            return "void";
        }
        String name = cls.getName();
        int p = name.lastIndexOf('.');
        String ret;
        if (p >= 0) {
            String classPack = name.substring(0, p);
            boolean hasPack = false;
            if (JAVA_LANG_PREFIX.equals(classPack)) {
                hasPack = true;
            } else if (packs != null) {
                for (String pack : packs) {
                    if (pack.equals(classPack)) {
                        hasPack = true;
                        break;
                    }
                }
            }
            if (hasPack) {
                ret = name.substring(p + 1);
            } else {
                ret = name;
            }
        } else {
            ret = name;
        }
        return ret.replace('$', '.');
    }

    protected static final class FetchClass {

        public final Class<?> cls;
        public final String fetchMethod;

        public FetchClass(Class<?> cls, String fetchMethod) {
            this.cls = cls;
            this.fetchMethod = fetchMethod;
        }
    }

    protected enum DecimalType {
        FLOAT, INT, LONG
    }

    protected DecimalType getDecimalType(int scale, int precision) {
        // todo: Oracle-specific
        if (scale == -127 || scale > 0 || precision == 0) {
            return DecimalType.FLOAT;
        } else {
            int len = precision - scale;
            if (len >= 20) {
                return DecimalType.FLOAT;
            } else if (len >= 10) {
                return DecimalType.LONG;
            } else {
                return DecimalType.INT;
            }
        }
    }

    /**
     * Defines Java numeric type to use for mapping.
     */
    protected FetchClass getDecimalClass(int scale, int precision, boolean notNull) {
        DecimalType integer = getDecimalType(scale, precision);
        switch (integer) {
        case INT:
            return notNull ? new FetchClass(Integer.TYPE, "getInt") : new FetchClass(Integer.class, "getINT");
        case LONG:
            return notNull ? new FetchClass(Long.TYPE, "getLong") : new FetchClass(Long.class, "getLONG");
        default:
            return notNull ? new FetchClass(Double.TYPE, "getDouble") : new FetchClass(Double.class, "getDOUBLE");
        }
    }

    protected FetchClass getDateClass() {
        return new FetchClass(java.sql.Date.class, "getDate");
    }

    /**
     * Defines Java non-numeric type to use for mapping.
     */
    protected FetchClass getPrimitiveNonDecimalClass(int jdbcType, boolean notNull) {
        switch (jdbcType) {
        case Types.BIGINT:
            if (notNull)
                return new FetchClass(Long.TYPE, "getLong");
            return new FetchClass(Long.class, "getLONG");
        case Types.INTEGER:
            if (notNull)
                return new FetchClass(Integer.TYPE, "getInt");
            return new FetchClass(Integer.class, "getINT");
        case Types.SMALLINT:
        case Types.TINYINT:
            if (notNull)
                return new FetchClass(Short.TYPE, "getShort");
            return new FetchClass(Short.class, "getSHORT");
        case Types.DOUBLE:
            if (notNull)
                return new FetchClass(Double.TYPE, "getDouble");
            return new FetchClass(Double.class, "getDOUBLE");
        case Types.FLOAT:
        case Types.REAL:
            if (notNull)
                return new FetchClass(Float.TYPE, "getFloat");
            return new FetchClass(Float.class, "getFLOAT");
        case Types.DATE:
            return getDateClass();
        case Types.TIME:
            return new FetchClass(Time.class, "getTime");
        case Types.TIMESTAMP:
            return new FetchClass(Timestamp.class, "getTimestamp");
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NVARCHAR:
            return new FetchClass(String.class, "getString");
        case Types.CLOB:
        case Types.NCLOB:
            return new FetchClass(String.class, "getCLOB");
        case Types.BLOB:
            return new FetchClass(byte[].class, "getBLOB");
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return new FetchClass(byte[].class, "getBytes");
        case Types.BIT:
        case Types.BOOLEAN:
            if (notNull)
                return new FetchClass(Boolean.TYPE, "getBoolean");
            return new FetchClass(Boolean.class, "getBOOLEAN");
        }
        return null;
    }

    /**
     * Returns Java type corresponding to DB column type.
     * @param rsmd result set metadata
     * @param col column index (from 1)
     * @return Java class
     */
    protected FetchClass getJavaType(ResultSetMetaData rsmd, int col) throws SQLException {
        boolean notNull = rsmd.isNullable(col) == ResultSetMetaData.columnNoNulls;
        int jdbcType = rsmd.getColumnType(col);
        switch (jdbcType) {
        case Types.DECIMAL:
        case Types.NUMERIC:
            {
                int precision = rsmd.getPrecision(col);
                int scale = rsmd.getScale(col);
                return getDecimalClass(scale, precision, notNull);
            }
        default:
            {
                FetchClass cls = getPrimitiveNonDecimalClass(jdbcType, notNull);
                if (cls != null) {
                    return cls;
                }
            }
        }
        return new FetchClass(Object.class, "getObject");
    }

    /**
     * Converts column name to Java method name.
     * @param columnName column name
     * @return Java method name
     */
    protected String getFieldName(String columnName) {
        StringBuilder buf = new StringBuilder(columnName.length());
        boolean lower = true;
        for (int i = 0; i < columnName.length(); i++) {
            char c = columnName.charAt(i);
            if (c == '_') {
                lower = false;
                continue;
            }
            if (lower) {
                buf.append(Character.toLowerCase(c));
            } else {
                buf.append(Character.toUpperCase(c));
            }
            lower = true;
        }
        return buf.toString();
    }

    protected String getColumnName(ResultSetMetaData rsmd, int column) throws SQLException {
        return rsmd.getColumnLabel(column);
    }

    /**
     * Defines mapping between simple columns and Java code (field name, type and fetch method).
     */
    protected ColumnInfo getSimpleColumnInfo(ResultSetMetaData rsmd, int j, String columnName,
                                             String resultSetVar, boolean meta) throws SQLException {
        FetchClass type = getJavaType(rsmd, j);
        String name = getFieldName(columnName);
        String fetchMethod = meta ? "getMeta" : type.fetchMethod;
        String method = fetchMethod + "(" + resultSetVar + ", " + j + ")";
        return new ColumnInfo(type.cls, name, method, false);
    }

    private ColumnInfo createSpecialColumn(SpecialType type, int from, int to, String specialName,
                                           String resultSetVar, String baseVar) {
        String method = baseVar + ".fetch(" + getClassName(type.cls, null) + ".class, " + resultSetVar + ", " + from + ", " + to + ")";
        return new ColumnInfo(type.cls, specialName, method, true);
    }

    public List<ColumnInfo> getFields(ResultSetMetaData rsmd, String resultSetVar, String baseVar, boolean meta) throws SQLException {
        int count = rsmd.getColumnCount();
        List<ColumnInfo> columns = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int j = i + 1;
            String columnName = getColumnName(rsmd, j);
            if (columnName.startsWith(QueryParser.SPECIAL)) {
                if (meta)
                    throw new SQLGException("Cannot use custom mapping in meta query");
                String converter = columnName.substring(QueryParser.SPECIAL.length());
                int p = converter.indexOf('#');
                String specialName;
                String specialType;
                if (p >= 0) {
                    specialName = converter.substring(0, p);
                    specialType = converter.substring(p + 1);
                } else {
                    specialName = null;
                    specialType = converter;
                }
                QueryParser.Range range = QueryParser.Range.parseRange(specialType);
                if (specialName == null) {
                    if (range.from == range.to) {
                        specialName = getColumnName(rsmd, range.from);
                    } else {
                        specialName = range.name;
                    }
                }
                specialName = getFieldName(specialName);
                specialType = range.name;
                SpecialType type = getSpecialType(specialType);
                if (type == null)
                    throw new SQLGException("Cannot map " + specialType);
                int from = range.from;
                int to = range.to;
                int paramCount = range.to - range.from + 1;
                if (type.paramCount >= 0 && type.paramCount != paramCount)
                    throw new SQLGException("Wrong number of parameters for " + specialType + ": expected " + type.paramCount + ", actual " + paramCount);
                columns.set(from - 1, createSpecialColumn(type, from, to, specialName, resultSetVar, baseVar));
                for (int k = from; k < to; k++) {
                    columns.set(k, null);
                }
            } else {
                columns.add(getSimpleColumnInfo(rsmd, j, columnName, resultSetVar, meta));
            }
        }
        List<ColumnInfo> ret = new ArrayList<>(columns.size());
        for (ColumnInfo ci : columns) {
            if (ci != null) {
                ret.add(ci);
            }
        }
        return ret;
    }

    /**
     * Defines mapping between custom-mapped columns and Java code (field name, type and fetch method).
     */
    protected SpecialType getSpecialType(String type) {
        return new SpecialType(-1, Object.class);
    }

    public Class<?> getParameterClass(Class<?> cls) {
        return cls;
    }
}
