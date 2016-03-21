package sqlg2;

/**
 * Description of DB-to-Java mapping for one column of a result set.
 */
public final class ColumnInfo {

    final Class<?> type;
    final String name;
    final String fetchMethod;
    final boolean special;

    /**
     * Constructor.
     *
     * @param type type of the Java field for this DB column
     * @param name name of the Java field for this DB column
     * @param fetchMethod method to retrieve info from result set for this DB column. It is a string containing
     * full text of a call using <code>resultSetVar</code> and <code>baseVar</code> parameters of
     * {@link Mapper#getFields(java.sql.ResultSetMetaData, String, String, boolean, sqlg2.checker.SqlChecker)} method.
     * <p>
     * Example: to retrieve <code>int</code> value from the first column of a result set you can create following
     * string for <code>fetchMethod</code> value:
     * <pre>
     * String fetchMethod = resultSetVar + ".getInt(1)";<br>
     * </pre>
     * <p>
     * Normally you would use {@link sqlg2.db.Impl} methods to retrieve data from result set.
     * @param special true if it is a custom-mapped class.
     */
    public ColumnInfo(Class<?> type, String name, String fetchMethod, boolean special) {
        this.type = type;
        this.name = name;
        this.fetchMethod = fetchMethod;
        this.special = special;
    }

    public String toString() {
        return type + " " + name + " " + fetchMethod;
    }
}
