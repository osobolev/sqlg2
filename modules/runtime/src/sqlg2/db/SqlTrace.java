package sqlg2.db;

/**
 * Tracing of SQL statements.
 */
public abstract class SqlTrace {

    public static final SqlTrace DEFAULT_TRACE = new SqlTrace() {
        public String getTraceMessage(boolean ok, long time) {
            return ok ? null : "SQL not completed properly";
        }
    };

    /**
     * @param ok false if SQL statement has not completed successfully
     * @param time time in milliseconds
     * @return null for no trace, not null for message to be output
     */
    public abstract String getTraceMessage(boolean ok, long time);
}
