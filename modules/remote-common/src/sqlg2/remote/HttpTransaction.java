package sqlg2.remote;

import sqlg2.db.ITransaction;
import sqlg2.db.RemoteException;

import java.sql.SQLException;

public final class HttpTransaction extends HttpSimpleTransaction implements ITransaction {

    public HttpTransaction(HttpId id, HttpProxy root) {
        super(id, root, HttpCommand.INVOKE);
    }

    public void rollback() throws SQLException {
        try {
            httpInvoke(HttpCommand.ROLLBACK);
        } catch (SQLException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }

    public void commit() throws SQLException {
        try {
            httpInvoke(HttpCommand.COMMIT);
        } catch (SQLException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RemoteException(ex);
        }
    }
}
