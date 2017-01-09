package sqlg2.db;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class performs Java/SQL mapping for given custom-mapped class.
 *
 * @param <T> custom-mapped class
 */
public abstract class CustomMapper<T> {

    /**
     * Fetches custom-mapped class from result set. This method is called only for columns for which
     * {@link sqlg2.Mapper#getFields} sets {@link sqlg2.ColumnInfo#special} flag. If custom-mapped class
     * is used only for parameters then this method can do anything.
     *
     * @param rs result set
     * @param from first column of result set
     * @param to last column of result set
     * @param cls custom-mapped class
     * @return instance of custom-mapped class fetched from result set
     */
    public abstract T fetch(ResultSet rs, int from, int to, Class<T> cls) throws SQLException;

    /**
     * Sets parameter of the custom-mapped class for prepared statement. If custom-mapped class
     * is not used for parameters then this method can do anything.
     *
     * @param stmt prepared statement
     * @param index parameter index
     * @param value value to set. If value is nullable you should provide this case too.
     * @param cls custom-mapped class
     */
    public abstract void set(PreparedStatement stmt, int index, T value, Class<T> cls) throws SQLException;

    private void noOut(Class<T> cls) {
        throw new SQLGException("Out parameters of type " + cls.getCanonicalName() + " are not supported");
    }

    /**
     * Registers OUT parameter of custom-mapped class for callable statement. By default this method throws
     * runtime exception. Override this method to use custom-mapped classes as OUT parameters.
     *
     * @param cs callable statement
     * @param index parameter index
     * @param cls custom-mapped class
     */
    public void register(CallableStatement cs, int index, Class<T> cls) throws SQLException {
        noOut(cls);
    }

    /**
     * Retrieves OUT parameter of custom-mapped class from callable statement after execution.
     * By default this method throws runtime exception. Override this method to use custom-mapped classes as OUT parameters.
     *
     * @param cs callable statement
     * @param index parameter index
     * @param cls custom-mapped class
     */
    public T get(CallableStatement cs, int index, Class<T> cls) throws SQLException {
        noOut(cls);
        return null;
    }
}
