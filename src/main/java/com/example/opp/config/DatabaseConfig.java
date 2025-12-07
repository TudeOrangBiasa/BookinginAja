package com.example.opp.config;

public record DatabaseConfig(
    String host,
    int port,
    String database,
    String username,
    String password,
    int poolSize,
    int timeout
) {
    public static DatabaseConfig fromProperties() {
        return new DatabaseConfig(
            AppConfig.get("db.host", "localhost"),
            AppConfig.getInt("db.port", 5432),
            AppConfig.get("db.name", "postgres"),
            AppConfig.get("db.username", "postgres"),
            AppConfig.get("db.password", ""),
            AppConfig.getInt("db.pool.size", 10),
            AppConfig.getInt("db.pool.timeout", 30000)
        );
    }

    public String getJdbcUrl() {
        // Supabase PostgreSQL connection string
        return String.format(
            "jdbc:postgresql://%s:%d/%s?sslmode=require&prepareThreshold=0",
            host, port, database
        );
    }
}
