package sqlg2.db.specific;

import java.io.OutputStream;
import java.io.Writer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database-specific operations interface.
 */
public interface DBSpecific {

    /**
     * Gets next number from sequence
     *
     * @param sequence sequence name
     */
    long getNextId(Connection conn, String sequence) throws SQLException;

    /**
     * Frees CLOB data after return from stored procedure.
     */
    void freeClob(Connection conn, Clob clob) throws SQLException;

    /**
     * Frees BLOB data after return from stored procedure.
     */
    void freeBlob(Connection conn, Blob blob) throws SQLException;

    /**
     * Retrieves stream to write BLOB contents
     */
    OutputStream getBlobOutputStream(Blob blob) throws SQLException;

    /**
     * Retrieves stream to write CLOB contents
     */
    Writer getClobWriter(Clob clob) throws SQLException;

    /**
     * Returns checker of SQL statements for this DB. Invoked only at preprocess phase, so this class is not required
     * at run time. Class should have default constructor to instantiate.
     */
    String getCheckerClassName();

    /**
     * Returns type code to use for {@link java.sql.CallableStatement#registerOutParameter(int, int)} for
     * cursor OUT parameters.
     */
    int getResultSetType();
}
