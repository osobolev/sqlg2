package sqlg2.db;

import java.io.Serializable;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Represents column meta-info.
 */
public final class MetaColumn implements Serializable {

    /**
     * true if column is not null
     */
    public final boolean notNull;
    /**
     * For VARCHAR/VARBINARY column types - maximum length
     */
    public final int length;
    /**
     * For NUMERIC column types - precision
     */
    public final int precision;
    /**
     * For NUMERIC column types - scale
     */
    public final int scale;

    public MetaColumn(ResultSetMetaData rsmd, int index) throws SQLException {
        notNull = rsmd.isNullable(index) == ResultSetMetaData.columnNoNulls;
        length = rsmd.getColumnDisplaySize(index);
        precision = rsmd.getPrecision(index);
        scale = rsmd.getScale(index);
    }

    public int length() {
        return length;
    }
}
