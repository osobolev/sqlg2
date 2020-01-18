package sqlg2.db;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;

/**
 * This class is used to get data from {@link ResultSet}.
 */
public class Impl {

    public static int getInt(ResultSet rs, int col) throws SQLException {
        return rs.getInt(col);
    }

    public static Integer getINT(ResultSet rs, int col) throws SQLException {
        int ret = rs.getInt(col);
        if (rs.wasNull())
            return null;
        return ret;
    }

    public static short getShort(ResultSet rs, int col) throws SQLException {
        return rs.getShort(col);
    }

    public static Short getSHORT(ResultSet rs, int col) throws SQLException {
        short ret = rs.getShort(col);
        if (rs.wasNull())
            return null;
        return ret;
    }

    public static long getLong(ResultSet rs, int col) throws SQLException {
        return rs.getLong(col);
    }

    public static Long getLONG(ResultSet rs, int col) throws SQLException {
        long ret = rs.getLong(col);
        if (rs.wasNull())
            return null;
        return ret;
    }

    public static byte getByte(ResultSet rs, int col) throws SQLException {
        return rs.getByte(col);
    }

    public static Byte getBYTE(ResultSet rs, int col) throws SQLException {
        byte ret = rs.getByte(col);
        if (rs.wasNull())
            return null;
        return ret;
    }

    public static float getFloat(ResultSet rs, int col) throws SQLException {
        return rs.getFloat(col);
    }

    public static Float getFLOAT(ResultSet rs, int col) throws SQLException {
        float ret = rs.getFloat(col);
        if (rs.wasNull())
            return null;
        return ret;
    }

    public static double getDouble(ResultSet rs, int col) throws SQLException {
        return rs.getDouble(col);
    }

    public static Double getDOUBLE(ResultSet rs, int col) throws SQLException {
        double ret = rs.getDouble(col);
        if (rs.wasNull())
            return null;
        return ret;
    }

    public static boolean getBoolean(ResultSet rs, int col) throws SQLException {
        return rs.getBoolean(col);
    }

    public static Boolean getBOOLEAN(ResultSet rs, int col) throws SQLException {
        boolean ret = rs.getBoolean(col);
        if (rs.wasNull())
            return null;
        return ret;
    }

    public static String getString(ResultSet rs, int col) throws SQLException {
        return rs.getString(col);
    }

    public static java.sql.Date getDate(ResultSet rs, int col) throws SQLException {
        return rs.getDate(col);
    }

    public static Time getTime(ResultSet rs, int col) throws SQLException {
        return rs.getTime(col);
    }

    public static Timestamp getTimestamp(ResultSet rs, int col) throws SQLException {
        return rs.getTimestamp(col);
    }

    public static BigDecimal getBigDecimal(ResultSet rs, int col) throws SQLException {
        return rs.getBigDecimal(col);
    }

    public static byte[] getBytes(ResultSet rs, int col) throws SQLException {
        return rs.getBytes(col);
    }

    public static String getCLOB(Clob clob) throws SQLException {
        if (clob == null)
            return null;
        Reader rdr = clob.getCharacterStream();
        if (rdr == null)
            return null;
        try {
            StringBuilder buf = new StringBuilder();
            while (true) {
                int c = rdr.read();
                if (c < 0)
                    break;
                buf.append((char) c);
            }
            return buf.toString();
        } catch (IOException ex) {
            throw wrap(ex);
        } finally {
            try {
                rdr.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    public static String getCLOB(ResultSet rs, int col) throws SQLException {
        Clob clob = rs.getClob(col);
        return getCLOB(clob);
    }

    public static byte[] getBLOB(Blob blob) throws SQLException {
        if (blob == null)
            return null;
        InputStream rdr = blob.getBinaryStream();
        if (rdr == null)
            return null;
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            while (true) {
                int c = rdr.read();
                if (c < 0)
                    break;
                buf.write(c);
            }
            return buf.toByteArray();
        } catch (IOException ex) {
            throw wrap(ex);
        } finally {
            try {
                rdr.close();
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    public static byte[] getBLOB(ResultSet rs, int col) throws SQLException {
        Blob blob = rs.getBlob(col);
        return getBLOB(blob);
    }

    public static Object getObject(ResultSet rs, int col) throws SQLException {
        return rs.getObject(col);
    }

    public static MetaColumn getMeta(ResultSet rs, int col) throws SQLException {
        return new MetaColumn(rs.getMetaData(), col);
    }

    public static SQLException wrap(String message, Throwable ex) {
        return new SQLException(message, ex);
    }

    public static SQLException wrap(Throwable ex) {
        return new SQLException(ex.toString(), ex);
    }
}
