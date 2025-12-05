package com.example.opp.database;

import com.example.opp.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Simple health check untuk database connection
 */
public final class DatabaseHealthCheck {

    private DatabaseHealthCheck() {}

    public static boolean isHealthy() {
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            if (!db.isConnected()) {
                db.connect();
            }
            
            Connection conn = db.getConnection();
            boolean valid = conn.isValid(2);
            db.releaseConnection(conn);
            
            return valid;
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getStatus() {
        try {
            DatabaseManager db = DatabaseManager.getInstance();
            if (!db.isConnected()) {
                db.connect();
            }

            Connection conn = db.getConnection();
            boolean valid = conn.isValid(2);
            String catalog = conn.getCatalog();
            db.releaseConnection(conn);

            return valid ? "Connected to: " + catalog : "Connection invalid";
        } catch (SQLException e) {
            return "Error: " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        System.out.println("Database Health Check");
        System.out.println("=====================");
        
        DatabaseConfig config = DatabaseConfig.fromProperties();
        System.out.println("Host: " + config.host() + ":" + config.port());
        System.out.println("Database: " + config.database());
        System.out.println("Username: " + config.username());
        System.out.println();
        
        System.out.print("Testing connection... ");
        if (isHealthy()) {
            System.out.println("✓ SUCCESS");
            System.out.println(getStatus());
        } else {
            System.out.println("✗ FAILED");
            System.out.println(getStatus());
            System.exit(1);
        }
    }
}
