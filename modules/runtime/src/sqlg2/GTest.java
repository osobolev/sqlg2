package sqlg2;

import sqlg2.db.RuntimeMapper;

import java.math.BigDecimal;
import java.sql.*;

public abstract class GTest {

    public static final Byte BYTE = 1;
    public static final Short SHORT = 1;
    public static final Integer INT = 1;
    public static final Long LONG = 1L;
    public static final Float FLOAT = 1.0f;
    public static final Double DOUBLE = 1.0;
    public static final BigDecimal BIG_DECIMAL = BigDecimal.ONE;
    public static final Character CHAR = '1';
    public static final String STRING = "1";
    public static final Boolean BOOLEAN = true;

    public static void setTest(GTest test) {
        GBase.test = test;
    }

    public static Date dateValue() {
        return new Date(System.currentTimeMillis());
    }

    public static Time timeValue() {
        return new Time(System.currentTimeMillis());
    }

    public static Timestamp timestampValue() {
        return new Timestamp(System.currentTimeMillis());
    }

    public static java.util.Date javaDateValue() {
        return new java.util.Date(System.currentTimeMillis());
    }

    public static byte[] bytesValue() {
        return new byte[] {1};
    }

    public static Object objectValue() {
        return new Object();
    }

    public abstract Object getValue(Class<?> cls, Integer jdbcType);

    public abstract <T> T getNullInterface(Class<T> iface);

    public abstract Object getTestObject(Class<?> paramType);

    public abstract void getFields(Class<?> retClass, ResultSet rs, boolean meta) throws SQLException;

    public abstract void checkOneColumn(ResultSet rs, Class<?> cls, boolean special) throws SQLException;

    public abstract void checkSql(String sql) throws SQLException;

    public abstract void checkSql(PreparedStatement stmt) throws SQLException;

    public abstract void statementCreated(Statement stmt, String sql);

    public abstract Class<?> setParamType(String paramId, Class<?> paramClass);

    public abstract void checkStoredProcName(String procNameToCall, Parameter[] parameters) throws SQLException;

    public abstract void checkSequenceExists(String sequence) throws SQLException;

    public abstract RuntimeMapper getRuntimeMapper();
}
