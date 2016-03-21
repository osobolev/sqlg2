package sqlg2.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * For internal use.
 * Stores methods to create row type class instances from result set.
 */
public final class RowTypeFactory {

    private final Constructor<?> cons;
    private final Method method;

    public RowTypeFactory(Constructor<?> cons, Method method) {
        this.cons = cons;
        this.method = method;
    }

    public Object call(Object[] params) throws SQLException {
        if (method != null) {
            return callMethod(method, params);
        } else {
            return callConstructor(cons, params);
        }
    }

    private static Object callConstructor(Constructor<?> cons, Object[] params) throws SQLException {
        Throwable cause;
        try {
            return cons.newInstance(params);
        } catch (InstantiationException ie) {
            cause = ie;
        } catch (IllegalAccessException iae) {
            cause = iae;
        } catch (InvocationTargetException ite) {
            Throwable target = ite.getTargetException();
            if (target instanceof SQLException)
                throw (SQLException) target;
            cause = target;
        }
        throw new SQLGException("Error while calling constructor", cause);
    }

    private static Object callMethod(Method method, Object[] params) throws SQLException {
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
