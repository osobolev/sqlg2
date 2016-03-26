package sqlg2.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * For internal use.
 * Stores methods to create row type class instances from result set.
 */
public final class RowTypeFactory {

    private final Method method;

    public RowTypeFactory(Method method) {
        this.method = method;
    }

    public Object call(Object[] params) throws SQLException {
        Throwable cause;
        try {
            return method.invoke(null, params);
        } catch (IllegalAccessException iae) {
            cause = iae;
        } catch (InvocationTargetException ite) {
            Throwable target = ite.getTargetException();
            if (target instanceof SQLException)
                throw (SQLException) target;
            cause = target;
        }
        throw new SQLGException("Error while calling factory method", cause);
    }
}
