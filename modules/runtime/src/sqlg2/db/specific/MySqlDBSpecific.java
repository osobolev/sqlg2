package sqlg2.db.specific;

/**
 * {@link DBSpecific} implementation for MySQL.
 */
public final class MySqlDBSpecific extends NoDBSpecific {

    public String getCheckerClassName() {
        return "sqlg2.checker.MySqlChecker";
    }
}
