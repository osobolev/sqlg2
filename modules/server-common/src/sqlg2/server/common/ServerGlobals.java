package sqlg2.server.common;

import sqlg2.db.Caches;
import sqlg2.db.SQLGLogger;
import sqlg2.db.SqlTrace;
import sqlg2.db.specific.DBSpecific;

public final class ServerGlobals {

    final DBSpecific specific;
    final Caches caches = new Caches();
    private final SQLGLogger logger;
    volatile SqlTrace trace = SqlTrace.DEFAULT_TRACE;

    public ServerGlobals(DBSpecific specific, SQLGLogger logger) {
        this.specific = specific;
        this.logger = logger;
    }

    public void setSqlTrace(SqlTrace trace) {
        this.trace = trace;
    }

    public SQLGLogger getLogger() {
        return logger;
    }
}
