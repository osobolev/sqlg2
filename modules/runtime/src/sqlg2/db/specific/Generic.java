package sqlg2.db.specific;

import sqlg2.db.DBSpecific;

import java.io.OutputStream;
import java.io.Writer;
import java.sql.*;

/**
 * {@link DBSpecific} implementation for generic JDBC database.
 */
public class Generic implements DBSpecific {

    public long getNextId(Connection conn, String sequence) throws SQLException {
        throw new SQLException("Database does not support sequences");
    }

    public void freeClob(Connection conn, Clob clob) throws SQLException {
        clob.free();
    }

    public void freeBlob(Connection conn, Blob blob) throws SQLException {
        blob.free();
    }

    public OutputStream getBlobOutputStream(Blob blob) throws SQLException {
        return blob.setBinaryStream(1);
    }

    public Writer getClobWriter(Clob clob) throws SQLException {
        return clob.setCharacterStream(1);
    }

    public String getCheckerClassName() {
        return "sqlg2.checker." + getClass().getSimpleName();
    }

    public int getResultSetType() {
        return Types.JAVA_OBJECT;
    }
}
