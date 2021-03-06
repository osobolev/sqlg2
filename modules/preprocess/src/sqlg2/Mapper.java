package sqlg2;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

/**
 * Preprocessor mapper interface. Maps DB types to Java types.
 */
public interface Mapper {

    /**
     * Creates test object used as a parameter for business method during preprocess.
     *
     * @param paramType Java type of a parameter
     * @return test object to be used for this parameter in a method invocation
     * done by preprocessor
     */
    Object getTestObject(Class<?> paramType);

    /**
     * Converts class to its name regarding import lists. Can be used to customize preprocessor output of class names.
     *
     * @param cls class
     * @param packs imported packages
     * @return class name to be used in preprocess result
     */
    String getClassName(Class<?> cls, String[] packs);

    /**
     * Retrieves column info from {@link java.sql.ResultSet}. Column info contains (for each query column):
     * <ul>
     * <li>Java class for this column
     * <li>Java field name
     * <li>Method to retrieve object of this class from result set
     * </ul>
     * See also {@link ColumnInfo}.
     *
     * @param rsmd result set meta-data
     * @param resultSetVar name of a variable which is used as result set (variable has type {@link java.sql.ResultSet}).
     * Should be used when generating value for {@link ColumnInfo#fetchMethod}.
     * @param baseVar name of a variable which is of {@link GBase} class used to perform SQL-to-Java mapping
     * @param meta true if extract meta-info from result set
     */
    List<ColumnInfo> getFields(ResultSetMetaData rsmd, String resultSetVar, String baseVar, boolean meta) throws SQLException;

    /**
     * Returns type of the query parameter when actual parameter type is {@code cls}. This method is called by
     * preprocessor when it encounters IN or OUT parameter of a query. The value passed to the parameter (usually
     * the value is generated by {@link #getTestObject}) has some runtime class, and sometimes you want the formal
     * type of the parameter differ from the actual runtime class.
     * <p>
     * For example, you have interface Ident and its implementation IntIdent, and want to use interface where possible,
     * so for runtime class IntIdent you can return Ident.class here.
     */
    Class<?> getParameterClass(Class<?> cls);
}
