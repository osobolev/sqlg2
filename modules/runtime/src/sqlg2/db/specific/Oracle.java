package sqlg2.db.specific;

import sqlg2.db.DBSpecific;
import sqlg2.db.Impl;

import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;

/**
 * {@link DBSpecific} implementation for Oracle.
 */
public final class Oracle implements DBSpecific {

    private final Method getBinaryOutputStream;
    private final Method getCharacterOutputStream;
    private final int resultSetType;

    public Oracle() {
        Method getBinaryOutputStream;
        try {
            getBinaryOutputStream = Class.forName("oracle.sql.BLOB").getMethod("getBinaryOutputStream");
        } catch (Exception ex) {
            getBinaryOutputStream = null;
        }
        Method getCharacterOutputStream;
        try {
            getCharacterOutputStream = Class.forName("oracle.sql.CLOB").getMethod("getCharacterOutputStream");
        } catch (Exception ex) {
            getCharacterOutputStream = null;
        }
        int cursor;
        try {
            Field field = Class.forName("oracle.jdbc.OracleTypes").getField("CURSOR");
            cursor = field.getInt(null);
        } catch (Exception ex) {
            cursor = -10;
        }
        this.resultSetType = cursor;
        this.getBinaryOutputStream = getBinaryOutputStream;
        this.getCharacterOutputStream = getCharacterOutputStream;
    }

    public static String getNextSeqSql(String sequence) {
        return "SELECT " + sequence + ".NEXTVAL FROM DUAL";
    }

    public long getNextId(Connection conn, String sequence) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.prepareStatement(getNextSeqSql(sequence));
            rs = stmt.executeQuery();
            rs.next();
            return rs.getLong(1);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
        }
    }

    public void freeClob(Connection conn, Clob clob) throws SQLException {
        CallableStatement clobStmt = null;
        try {
            clobStmt = conn.prepareCall("BEGIN IF dbms_lob.isTemporary(?) <> 0 THEN dbms_lob.freeTemporary(?); END IF; END;");
            clobStmt.setClob(1, clob);
            clobStmt.setClob(2, clob);
            clobStmt.executeUpdate();
        } finally {
            if (clobStmt != null) {
                try {
                    clobStmt.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
        }
    }

    public void freeBlob(Connection conn, Blob blob) throws SQLException {
        CallableStatement blobStmt = null;
        try {
            blobStmt = conn.prepareCall("BEGIN IF dbms_lob.isTemporary(?) <> 0 THEN dbms_lob.freeTemporary(?); END IF; END;");
            blobStmt.setBlob(1, blob);
            blobStmt.setBlob(2, blob);
            blobStmt.executeUpdate();
        } finally {
            if (blobStmt != null) {
                try {
                    blobStmt.close();
                } catch (SQLException ex) {
                    // ignore
                }
            }
        }
    }

    private static Object invokeLobMethod(Method method, Object lob) throws SQLException {
        try {
            return method.invoke(lob);
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() instanceof SQLException)
                throw (SQLException) ex.getTargetException();
            throw Impl.wrap(ex);
        } catch (IllegalAccessException ex) {
            throw Impl.wrap(ex);
        }
    }

    public OutputStream getBlobOutputStream(Blob blob) throws SQLException {
        return (OutputStream) invokeLobMethod(getBinaryOutputStream, blob);
    }

    public Writer getClobWriter(Clob clob) throws SQLException {
        return (Writer) invokeLobMethod(getCharacterOutputStream, clob);
    }

    public String getCheckerClassName() {
        return "sqlg2.checker.Oracle";
    }

    public int getResultSetType() {
        return resultSetType;
    }
}
