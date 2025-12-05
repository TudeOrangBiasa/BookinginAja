package com.example.opp.database;

import com.example.opp.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool {

    private final BlockingQueue<Connection> pool;
    private final DatabaseConfig config;
    private boolean initialized = false;

    public ConnectionPool(DatabaseConfig config) {
        this.config = config;
        this.pool = new ArrayBlockingQueue<>(config.poolSize());
    }

    public void initialize() throws SQLException {
        if (initialized) return;

        for (int i = 0; i < config.poolSize(); i++) {
            pool.offer(createConnection());
        }
        initialized = true;
    }

    private Connection createConnection() throws SQLException {
        return DriverManager.getConnection(
            config.getJdbcUrl(),
            config.username(),
            config.password()
        );
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection conn = pool.poll(config.timeout(), TimeUnit.MILLISECONDS);
            if (conn == null) {
                throw new SQLException("Connection timeout - pool exhausted");
            }
            if (!conn.isValid(1)) {
                conn = createConnection();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection", e);
        }
    }

    public void releaseConnection(Connection conn) {
        if (conn != null) {
            pool.offer(conn);
        }
    }

    public void shutdown() {
        pool.forEach(conn -> {
            try {
                conn.close();
            } catch (SQLException ignored) {}
        });
        pool.clear();
        initialized = false;
    }
}
