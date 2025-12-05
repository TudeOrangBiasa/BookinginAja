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
            AppConfig.getInt("db.port", 3306),
            AppConfig.get("db.name", ""),
            AppConfig.get("db.username", "root"),
            AppConfig.get("db.password", ""),
            AppConfig.getInt("db.pool.size", 10),
            AppConfig.getInt("db.pool.timeout", 30000)
        );
    }

    public String getJdbcUrl() {
        return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC", 
            host, port, database);
    }
}
