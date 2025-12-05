package com.example.opp.database;

import com.example.opp.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseManager {

    private static DatabaseManager instance;
    private final ConnectionPool connectionPool;
    private boolean connected = false;

    private DatabaseManager(DatabaseConfig config) {
        this.connectionPool = new ConnectionPool(config);
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager(DatabaseConfig.fromProperties());
        }
        return instance;
    }

    public void connect() throws SQLException {
        if (connected) return;
        connectionPool.initialize();
        connected = true;
    }

    public Connection getConnection() throws SQLException {
        if (!connected) {
            throw new SQLException("Database not connected. Call connect() first.");
        }
        return connectionPool.getConnection();
    }

    public void releaseConnection(Connection conn) {
        connectionPool.releaseConnection(conn);
    }

    public void disconnect() {
        connectionPool.shutdown();
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public <T> T executeWithConnection(ConnectionCallback<T> callback) throws SQLException {
        Connection conn = getConnection();
        try {
            return callback.execute(conn);
        } finally {
            releaseConnection(conn);
        }
    }

    public void executeWithConnection(ConnectionVoidCallback callback) throws SQLException {
        Connection conn = getConnection();
        try {
            callback.execute(conn);
        } finally {
            releaseConnection(conn);
        }
    }

    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T execute(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    public interface ConnectionVoidCallback {
        void execute(Connection conn) throws SQLException;
    }
}
