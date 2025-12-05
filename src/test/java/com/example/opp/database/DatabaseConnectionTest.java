package com.example.opp.database;

import com.example.opp.config.AppConfig;
import com.example.opp.config.DatabaseConfig;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseConnectionTest {

    private static DatabaseManager dbManager;

    @BeforeAll
    static void setup() {
        AppConfig.load();
        dbManager = DatabaseManager.getInstance();
    }

    @Test
    @Order(1)
    @DisplayName("Should load database configuration")
    void testConfigurationLoading() {
        DatabaseConfig config = DatabaseConfig.fromProperties();
        
        assertNotNull(config.host());
        assertNotNull(config.database());
        assertNotNull(config.username());
        assertTrue(config.port() > 0);
        
        System.out.println("✓ Configuration loaded: " + config.getJdbcUrl());
    }

    @Test
    @Order(2)
    @DisplayName("Should connect to database")
    void testDatabaseConnection() throws Exception {
        dbManager.connect();
        assertTrue(dbManager.isConnected());
        
        System.out.println("✓ Database connected");
    }

    @Test
    @Order(3)
    @DisplayName("Should get valid connection from pool")
    void testConnectionPool() throws Exception {
        Connection conn = dbManager.getConnection();
        
        assertNotNull(conn);
        assertFalse(conn.isClosed());
        assertTrue(conn.isValid(2));
        
        dbManager.releaseConnection(conn);
        System.out.println("✓ Connection pool working");
    }

    @Test
    @Order(4)
    @DisplayName("Should retrieve database metadata")
    void testDatabaseMetadata() throws Exception {
        Connection conn = dbManager.getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        
        assertNotNull(metaData.getDatabaseProductName());
        assertNotNull(metaData.getDatabaseProductVersion());
        
        System.out.println("✓ Database: " + metaData.getDatabaseProductName() + 
                         " " + metaData.getDatabaseProductVersion());
        
        dbManager.releaseConnection(conn);
    }

    @Test
    @Order(5)
    @DisplayName("Should verify users table exists")
    void testUsersTableExists() throws Exception {
        Connection conn = dbManager.getConnection();
        var tables = conn.getMetaData().getTables(null, null, "users", null);
        
        assertTrue(tables.next(), "Users table should exist");
        System.out.println("✓ Users table exists");
        
        dbManager.releaseConnection(conn);
    }

    @AfterAll
    static void cleanup() {
        if (dbManager != null) {
            dbManager.disconnect();
            System.out.println("✓ Database disconnected");
        }
    }
}
