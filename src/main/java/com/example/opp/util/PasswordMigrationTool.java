package com.example.opp.util;

import com.example.opp.config.AppConfig;
import com.example.opp.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Tool untuk migrate plain text password ke hashed password
 * Run: mvn exec:java -Dexec.mainClass="com.example.opp.util.PasswordMigrationTool"
 */
public class PasswordMigrationTool {

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  PASSWORD MIGRATION TOOL");
        System.out.println("=================================\n");

        try {
            AppConfig.load();
            DatabaseManager db = DatabaseManager.getInstance();
            db.connect();

            Scanner scanner = new Scanner(System.in);
            
            System.out.println("Choose migration mode:");
            System.out.println("1. Hash all plain text passwords");
            System.out.println("2. Hash specific user password");
            System.out.println("3. Generate hash for new password");
            System.out.print("\nEnter choice (1-3): ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> hashAllPasswords(db);
                case 2 -> hashSpecificUser(db, scanner);
                case 3 -> generateHash(scanner);
                default -> System.out.println("Invalid choice");
            }

            db.disconnect();
            scanner.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void hashAllPasswords(DatabaseManager db) throws SQLException {
        System.out.println("\n⚠️  WARNING: This will hash ALL passwords in the database!");
        System.out.print("Are you sure? (yes/no): ");
        
        Scanner confirm = new Scanner(System.in);
        if (!confirm.nextLine().equalsIgnoreCase("yes")) {
            System.out.println("Operation cancelled.");
            return;
        }

        Connection conn = db.getConnection();
        
        // Get all users with plain text passwords (passwords without ':' separator)
        String selectSql = "SELECT id, username, password FROM users WHERE password NOT LIKE '%:%'";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            
            ResultSet rs = selectStmt.executeQuery();
            int count = 0;
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String username = rs.getString("username");
                String plainPassword = rs.getString("password");
                
                String hashedPassword = PasswordUtil.hash(plainPassword);
                
                updateStmt.setString(1, hashedPassword);
                updateStmt.setLong(2, id);
                updateStmt.executeUpdate();
                
                System.out.println("✓ Hashed password for user: " + username);
                count++;
            }
            
            System.out.println("\n✓ Successfully hashed " + count + " passwords");
            
        } finally {
            db.releaseConnection(conn);
        }
    }

    private static void hashSpecificUser(DatabaseManager db, Scanner scanner) throws SQLException {
        System.out.print("\nEnter username: ");
        String username = scanner.nextLine();
        
        System.out.print("Enter plain text password: ");
        String plainPassword = scanner.nextLine();
        
        String hashedPassword = PasswordUtil.hash(plainPassword);
        
        Connection conn = db.getConnection();
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashedPassword);
            stmt.setString(2, username);
            
            int updated = stmt.executeUpdate();
            
            if (updated > 0) {
                System.out.println("\n✓ Password updated for user: " + username);
                System.out.println("Hashed password: " + hashedPassword);
            } else {
                System.out.println("\n✗ User not found: " + username);
            }
            
        } finally {
            db.releaseConnection(conn);
        }
    }

    private static void generateHash(Scanner scanner) {
        System.out.print("\nEnter password to hash: ");
        String password = scanner.nextLine();
        
        String hashed = PasswordUtil.hash(password);
        
        System.out.println("\n=================================");
        System.out.println("Original: " + password);
        System.out.println("Hashed:   " + hashed);
        System.out.println("=================================");
        System.out.println("\nYou can use this hash in SQL:");
        System.out.println("UPDATE users SET password = '" + hashed + "' WHERE username = 'your_username';");
    }
}
