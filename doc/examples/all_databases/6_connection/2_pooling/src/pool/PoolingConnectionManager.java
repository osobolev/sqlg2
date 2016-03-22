package pool;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import sqlg2.db.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public final class PoolingConnectionManager implements ConnectionManager {

    private final BoneCP pool;

    public PoolingConnectionManager(String driver, String url, String username, String password) throws SQLException {
        BoneCPConfig config = new BoneCPConfig();
 	config.setJdbcUrl(url);
	config.setUsername(username);
	config.setPassword(password);
	pool = new BoneCP(config);
    }

    public Connection allocConnection() throws SQLException {
        return pool.getConnection();
    }

    public void releaseConnection(Connection conn) throws SQLException {
        conn.close();
    }

    public void commit(Connection conn) throws SQLException {
        conn.commit();
    }

    public void rollback(Connection conn) throws SQLException {
        conn.rollback();
    }

    public void close() throws SQLException {
        pool.shutdown();
    }
}
