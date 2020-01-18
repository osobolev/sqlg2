package sqlg2.db.client;

import sqlg2.db.ITransaction;
import sqlg2.db.RemoteException;
import sqlg2.db.remote.HttpCommand;
import sqlg2.db.remote.HttpId;

import java.sql.SQLException;

final class HttpTransaction extends HttpSimpleTransaction implements ITransaction {

    HttpTransaction(HttpRootObject rootObject, HttpId id) {
        super(rootObject, id, HttpCommand.INVOKE);
    }

    public void rollback() throws SQLException {
        try {
            rootObject.httpInvoke(Void.TYPE, HttpCommand.ROLLBACK, id);
        } catch (SQLException | RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    public void commit() throws SQLException {
        try {
            rootObject.httpInvoke(Void.TYPE, HttpCommand.COMMIT, id);
        } catch (SQLException | RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }
}
