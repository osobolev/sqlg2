package pool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.pool.HikariPool;
import sqlg2.db.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;

public final class PoolingConnectionManager implements ConnectionManager {

    private final HikariPool pool;

    public PoolingConnectionManager(String driver, String url, String username, String password) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setAutoCommit(false);
        pool = new HikariPool(config);
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
        try {
            pool.shutdown();
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
