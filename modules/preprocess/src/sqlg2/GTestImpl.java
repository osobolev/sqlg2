package sqlg2;

import sqlg2.db.DBSpecific;
import sqlg2.db.Impl;
import sqlg2.db.RuntimeMapper;
import sqlg2.db.SQLGException;
import sqlg2.queries.QueryParser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class GTestImpl extends GTest {

    static final GTestImpl INSTANCE = new GTestImpl();

    static final String RESULT_SET = "rs";
    static final String BASE = "base";

    Connection connection = null;
    private SqlChecker checker = null;
    Mapper mapper = null;
    private RuntimeMapper runtimeMapper = null;
    DBSpecific specific = null;

    List<ColumnInfo> columns = null;
    boolean meta;
    Class<?> returnClass = null;
    final Map<String, Class<?>> paramTypeMap = new HashMap<String, Class<?>>();
    Map<String, List<ParamCutPaste>> bindMap = null;
    private Map<Statement, String> stmtMap = null;

    private GTestImpl() {
    }

    void init(Connection connection, SqlChecker checker, Mapper mapper, RuntimeMapper runtimeMapper, DBSpecific specific) {
        this.connection = connection;
        this.checker = checker;
        this.mapper = mapper;
        this.runtimeMapper = runtimeMapper;
        this.specific = specific;
    }

    public Object getValue(Class<?> cls, Integer jdbcType) {
        if (cls != null)
            return getTestObject(cls);
        if (jdbcType == null)
            return null;
        switch (jdbcType.intValue()) {
        case Types.TINYINT:
            return BYTE;
        case Types.SMALLINT:
            return SHORT;
        case Types.INTEGER:
            return INT;
        case Types.BIGINT:
            return LONG;
        case Types.FLOAT:
            return FLOAT;
        case Types.REAL:
        case Types.DOUBLE:
            return DOUBLE;
        case Types.DECIMAL:
        case Types.NUMERIC:
            return BIG_DECIMAL;
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
            return STRING;
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return bytesValue();
        case Types.BOOLEAN:
            return BOOLEAN;
        case Types.DATE:
            return dateValue();
        case Types.TIME:
            return timeValue();
        case Types.TIMESTAMP:
            return timestampValue();
        case Types.CLOB:
            return STRING;
        case Types.BLOB:
            return bytesValue();
        default:
            return objectValue();
        }
    }

    public <T> T getNullInterface(Class<T> iface) {
        Object ret = Proxy.newProxyInstance(
            iface.getClassLoader(),
            new Class[] {iface},
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    return getValue(method.getReturnType(), null);
                }
            }
        );
        return iface.cast(ret);
    }

    public Object getTestObject(Class<?> paramType) {
        return mapper.getTestObject(paramType);
    }

    public void getFields(Class<?> retClass, ResultSet rs, boolean meta) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        columns = mapper.getFields(rsmd, RESULT_SET, BASE, meta);
        this.meta = meta;
        if (retClass != null) {
            returnClass = retClass;
        }
    }

    private ColumnInfo getOneColumn(ResultSet rs) throws SQLException {
        getFields(null, rs, false);
        if (columns == null || columns.size() != 1) {
            throw new SQLException("More than one column in result set");
        }
        ColumnInfo col1 = columns.get(0);
        columns = null;
        returnClass = null;
        return col1;
    }

    public void checkOneColumn(ResultSet rs, Class<?> cls, boolean special) throws SQLException {
        ColumnInfo col1 = getOneColumn(rs);
        if (special) {
            if (!col1.special)
                throw new SQLException("Column is not of special type, but of " + col1.type.getCanonicalName());
            if (!cls.equals(col1.type))
                throw new SQLException("Column is of type " + col1.type.getCanonicalName() + ", but " + cls.getCanonicalName() + " required");
        } else {
            if (col1.special)
                throw new SQLException("Column is of special type " + col1.type.getCanonicalName());
        }
    }

    public void checkSql(String sql) throws SQLException {
        if (sql == null)
            throw new SQLException("SQL is null");
        try {
            checker.checkSql(connection, sql);
        } catch (SQLException ex) {
            throw Impl.wrap("Invalid SQL: " + sql + "\n" + ex.getMessage(), ex);
        }
    }

    public void checkSql(PreparedStatement stmt) throws SQLException {
        checker.checkStatement(stmt);
        if (stmtMap == null)
            return;
        String sql = stmtMap.get(stmt);
        if (sql == null)
            return;
        checkSql(sql);
    }

    public void statementCreated(Statement stmt, String sql) {
        if (sql != null) {
            if (stmtMap == null) {
                stmtMap = new HashMap<Statement, String>();
            }
            stmtMap.put(stmt, sql);
        }
    }

    public Class<?> setParamType(String paramId, Class<?> paramClass) {
        Class<?> cls = paramClass == null ? null : mapper.getParameterClass(paramClass);
        List<ParamCutPaste> params = bindMap.get(paramId);
        if (params != null) {
            String className = cls == null ? null : mapper.getClassName(cls, null);
            for (ParamCutPaste param : params) {
                if (param.out) {
                    param.replaceTo = "outP(" + param.param + ")";
                } else {
                    if (className != null) {
                        param.replaceTo = "inP(" + param.param + ", " + className + ".class)";
                    }
                }
            }
        }
        if (paramTypeMap.containsKey(paramId)) {
            Class<?> existingClass = paramTypeMap.get(paramId);
            boolean same;
            if (cls == null) {
                same = existingClass == null;
            } else {
                same = cls.equals(existingClass);
            }
            if (!same) {
                throw new SQLGException("Parameter " + paramId + " has conflicting type definitions: was " + existingClass + ", became " + cls);
            }
        }
        paramTypeMap.put(paramId, cls);
        return cls;
    }

    private static String kindStr(boolean out) {
        return out ? "OUT" : "IN";
    }

    public void checkStoredProcName(String procNameToCall, Parameter[] parameters) throws SQLException {
        String[] parsed = QueryParser.parseIdent(procNameToCall);
        if (parsed == null)
            throw new SQLException("Invalid procedure name: " + procNameToCall);
        DatabaseMetaData meta = connection.getMetaData();
        String[] procSchemas;
        String[] procCatalogs;
        String procName;
        if (parsed.length == 1) {
            procSchemas = new String[] {checker.getCurrentSchema(meta)};
            procCatalogs = new String[] {null};
            procName = parsed[0];
        } else if (parsed.length == 2) {
            procSchemas = new String[] {checker.getCurrentSchema(meta), parsed[0]};
            procCatalogs = new String[] {parsed[0], null};
            procName = parsed[1];
        } else {
            procSchemas = new String[] {parsed[0]};
            procCatalogs = new String[] {parsed[1]};
            procName = parsed[2];
        }
        ResultSet rs = null;
        try {
            String procSchema;
            if (procSchemas.length > 1) {
                procSchema = null;
            } else {
                procSchema = procSchemas[0];
            }
            String procCatalog;
            if (procCatalogs.length > 1) {
                procCatalog = null;
            } else {
                procCatalog = procCatalogs[0];
            }
            rs = meta.getProcedures(procCatalog, procSchema, procName);
            String foundSchema = null;
            String foundCatalog = null;
            int foundCount = 0;
            Integer returnType = null;
            while (rs.next()) {
                String catalog = rs.getString(1);
                String schema = rs.getString(2);
                boolean found = false;
                for (int j = 0; j < procSchemas.length; j++) {
                    boolean schemaEqual;
                    if (schema == null || schema.length() <= 0) {
                        schemaEqual = procSchemas[j] == null || procSchemas[j].length() <= 0;
                    } else {
                        schemaEqual = schema.equals(procSchemas[j]);
                    }
                    if (!schemaEqual)
                        continue;
                    boolean catalogEqual;
                    if (catalog == null || catalog.length() <= 0) {
                        catalogEqual = procCatalogs[j] == null;
                    } else {
                        catalogEqual = catalog.equals(procCatalogs[j]);
                    }
                    if (!catalogEqual)
                        continue;
                    found = true;
                    break;
                }
                if (found) {
                    foundSchema = schema;
                    foundCatalog = catalog;
                    returnType = rs.getInt(8);
                    foundCount++;
                }
            }
            if (foundCount <= 0) {
                throw new SQLException("Procedure " + procNameToCall + " not found");
            }
            if (foundCount == 1) {
                if (returnType != null && returnType.intValue() == DatabaseMetaData.procedureReturnsResult)
                    throw new SQLException(procNameToCall + " is a function, not a procedure");
                rs.close();
                rs = meta.getProcedureColumns(foundCatalog, foundSchema, procName, null);
                int paramCount = 0;
                while (rs.next()) {
                    int paramKind = rs.getShort(5);
                    if (paramKind != DatabaseMetaData.procedureColumnUnknown) {
                        boolean needOut = paramKind == DatabaseMetaData.procedureColumnInOut || paramKind == DatabaseMetaData.procedureColumnOut;
                        boolean actualOut = parameters[paramCount].isOut();
                        if (needOut != actualOut) {
                            String paramName = rs.getString(4);
                            if (paramName == null) {
                                paramName = "_" + (paramCount + 1);
                            }
                            throw new SQLException("Parameter mismatch in call to " + procNameToCall + ": parameter " + paramName + " declared as " + kindStr(needOut) + " but passed as " + kindStr(actualOut));
                        }
                    }
                    paramCount++;
                }
                if (paramCount != parameters.length)
                    throw new SQLException("Parameter count does not match in call to " + procNameToCall + " (expected " + paramCount + " != actual " + parameters.length + ")");
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
        }
    }

    public void checkSequenceExists(String sequence) throws SQLException {
        checker.checkSequenceExists(connection, sequence);
    }

    public RuntimeMapper getRuntimeMapper() {
        return runtimeMapper;
    }

    void startCall() {
        columns = null;
        meta = false;
        stmtMap = null;
        returnClass = null;
    }

    static boolean isOutClass(Class<?> rowTypeClass) {
        if (rowTypeClass != null) {
            Class<?> outer = rowTypeClass.getDeclaringClass();
            return outer == null;
        } else {
            return false;
        }
    }

    static RowTypeCutPaste isInClass(Class<?> mainClass, Class<?> rowTypeClass, Map<String, RowTypeCutPaste> rowTypeMap) {
        if (rowTypeClass != null) {
            Class<?> outer = rowTypeClass.getDeclaringClass();
            if (outer != null && mainClass.getName().equals(outer.getName())) {
                String key = mainClass.getSimpleName() + "." + rowTypeClass.getSimpleName();
                return rowTypeMap.get(key);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
