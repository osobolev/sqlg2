package sqlg2.db.specific;

/**
 * {@link sqlg2.db.DBSpecific} implementation for MySQL.
 */
public final class MySqlDBSpecific extends NoDBSpecific {

    public String getCheckerClassName() {
        return "sqlg2.checker.MySqlChecker";
    }
}
