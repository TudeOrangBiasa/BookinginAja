package com.example.opp.database;

import com.example.opp.config.AppConfig;
import com.example.opp.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Database seeder for initial data setup
 * Run: mvn exec:java -Dexec.mainClass="com.example.opp.database.DatabaseSeeder"
 */
public class DatabaseSeeder {

    private final DatabaseManager db;

    public DatabaseSeeder() {
        this.db = DatabaseManager.getInstance();
    }

    public static void main(String[] args) {
        System.out.println("=================================");
        System.out.println("  DATABASE SEEDER");
        System.out.println("=================================\n");

        try {
            AppConfig.load();
            DatabaseSeeder seeder = new DatabaseSeeder();
            seeder.db.connect();
            
            seeder.seedAll();
            
            seeder.db.disconnect();
            System.out.println("\n✓ Seeding complete!");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void seedAll() throws SQLException {
        seedUsers();
        seedRoomTypes();
        seedRooms();
        seedSampleGuests();
    }

    private void seedUsers() throws SQLException {
        if (hasData("users")) {
            System.out.println("⏭ Users already exist, skipping...");
            return;
        }

        String sql = "INSERT INTO users (username, password, email, full_name, role, active) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = db.getConnection();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Admin user
            stmt.setString(1, "admin");
            stmt.setString(2, PasswordUtil.hash("admin123"));
            stmt.setString(3, "admin@hotel.com");
            stmt.setString(4, "Administrator");
            stmt.setString(5, "ADMIN");
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
            System.out.println("✓ Created admin user (password: admin123)");

            // Receptionist user
            stmt.setString(1, "receptionist");
            stmt.setString(2, PasswordUtil.hash("staff123"));
            stmt.setString(3, "receptionist@hotel.com");
            stmt.setString(4, "Front Desk Staff");
            stmt.setString(5, "RECEPTIONIST");
            stmt.setBoolean(6, true);
            stmt.executeUpdate();
            System.out.println("✓ Created receptionist user (password: staff123)");
        } finally {
            db.releaseConnection(conn);
        }
    }

    private void seedRoomTypes() throws SQLException {
        if (hasData("room_types")) {
            System.out.println("⏭ Room types already exist, skipping...");
            return;
        }

        String sql = "INSERT INTO room_types (name, description, base_price, capacity) VALUES (?, ?, ?, ?)";
        Connection conn = db.getConnection();
        
        Object[][] types = {
            {"Single", "Cozy single room with one bed", 350000, 1},
            {"Double", "Comfortable room with queen bed", 500000, 2},
            {"Twin", "Room with two single beds", 550000, 2},
            {"Deluxe", "Spacious room with premium amenities", 850000, 2},
            {"Suite", "Luxury suite with living area", 1500000, 4},
            {"Family", "Large room for family stays", 1200000, 5},
            {"Dormitory", "Budget-friendly shared room", 150000, 1}
        };

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] type : types) {
                stmt.setString(1, (String) type[0]);
                stmt.setString(2, (String) type[1]);
                stmt.setInt(3, (Integer) type[2]);
                stmt.setInt(4, (Integer) type[3]);
                stmt.executeUpdate();
            }
            System.out.println("✓ Created " + types.length + " room types");
        } finally {
            db.releaseConnection(conn);
        }
    }

    private void seedRooms() throws SQLException {
        if (hasData("rooms")) {
            System.out.println("⏭ Rooms already exist, skipping...");
            return;
        }

        String sql = "INSERT INTO rooms (room_number, room_type_id, floor, status, amenities) VALUES (?, ?, ?, ?, ?)";
        Connection conn = db.getConnection();
        
        Object[][] rooms = {
            {"101", 1L, 1, "AVAILABLE", "AC, TV, WiFi, Hot Water"},
            {"102", 1L, 1, "AVAILABLE", "AC, TV, WiFi, Hot Water"},
            {"103", 2L, 1, "AVAILABLE", "AC, TV, WiFi, Mini Bar"},
            {"104", 2L, 1, "AVAILABLE", "AC, TV, WiFi, Mini Bar"},
            {"201", 3L, 2, "AVAILABLE", "AC, TV, WiFi, Mini Bar"},
            {"202", 3L, 2, "AVAILABLE", "AC, TV, WiFi, Mini Bar"},
            {"203", 4L, 2, "AVAILABLE", "AC, Smart TV, WiFi, Bathtub"},
            {"204", 4L, 2, "MAINTENANCE", "AC, Smart TV, WiFi, Bathtub"},
            {"301", 5L, 3, "AVAILABLE", "AC, Smart TV, WiFi, Jacuzzi"},
            {"302", 5L, 3, "AVAILABLE", "AC, Smart TV, WiFi, Jacuzzi"},
            {"303", 6L, 3, "AVAILABLE", "AC, Smart TV, WiFi, 2 Bedrooms"},
            {"D01", 7L, 1, "AVAILABLE", "AC, Shared Bathroom, Locker"}
        };

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] room : rooms) {
                stmt.setString(1, (String) room[0]);
                stmt.setLong(2, (Long) room[1]);
                stmt.setInt(3, (Integer) room[2]);
                stmt.setString(4, (String) room[3]);
                stmt.setString(5, (String) room[4]);
                stmt.executeUpdate();
            }
            System.out.println("✓ Created " + rooms.length + " rooms");
        } finally {
            db.releaseConnection(conn);
        }
    }

    private void seedSampleGuests() throws SQLException {
        if (hasData("guests")) {
            System.out.println("⏭ Guests already exist, skipping...");
            return;
        }

        String sql = "INSERT INTO guests (id_number, id_type, full_name, phone, email, address) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = db.getConnection();
        
        Object[][] guests = {
            {"3201234567890001", "KTP", "Budi Santoso", "081234567890", "budi@email.com", "Jakarta"},
            {"3201234567890002", "KTP", "Siti Rahayu", "081234567891", "siti@email.com", "Bandung"},
            {"A12345678", "PASSPORT", "John Smith", "+1234567890", "john@email.com", "New York, USA"},
            {"3201234567890003", "KTP", "Ahmad Wijaya", "081234567892", "ahmad@email.com", "Surabaya"}
        };

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Object[] guest : guests) {
                stmt.setString(1, (String) guest[0]);
                stmt.setString(2, (String) guest[1]);
                stmt.setString(3, (String) guest[2]);
                stmt.setString(4, (String) guest[3]);
                stmt.setString(5, (String) guest[4]);
                stmt.setString(6, (String) guest[5]);
                stmt.executeUpdate();
            }
            System.out.println("✓ Created " + guests.length + " sample guests");
        } finally {
            db.releaseConnection(conn);
        }
    }

    private boolean hasData(String table) throws SQLException {
        Connection conn = db.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM " + table)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } finally {
            db.releaseConnection(conn);
        }
    }
}
