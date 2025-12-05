-- ============================================
-- Hotel Management System - Database Schema
-- Version: 1.0
-- Compatible with: MySQL 8.0+
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS hotel_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hotel_management;

-- ============================================
-- 1. USERS TABLE (Authentication & Authorization)
-- ============================================
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS guests;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS room_types;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'RECEPTIONIST') NOT NULL DEFAULT 'RECEPTIONIST',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_username (username),
    INDEX idx_users_role (role),
    INDEX idx_users_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 2. ROOM TYPES TABLE
-- ============================================
CREATE TABLE room_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    base_price DECIMAL(12,2) NOT NULL,
    capacity INT NOT NULL DEFAULT 2,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_room_types_name UNIQUE (name),
    CONSTRAINT chk_room_types_price CHECK (base_price >= 0),
    CONSTRAINT chk_room_types_capacity CHECK (capacity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 3. ROOMS TABLE
-- ============================================
CREATE TABLE rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL,
    room_type_id BIGINT NOT NULL,
    floor INT NOT NULL DEFAULT 1,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    amenities TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_rooms_number UNIQUE (room_number),
    CONSTRAINT fk_rooms_type FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT,
    CONSTRAINT chk_rooms_floor CHECK (floor > 0),
    INDEX idx_rooms_status (status),
    INDEX idx_rooms_number (room_number),
    INDEX idx_rooms_floor (floor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 4. GUESTS TABLE
-- ============================================
CREATE TABLE guests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_number VARCHAR(50) NOT NULL,
    id_type ENUM('KTP', 'PASSPORT', 'SIM') NOT NULL DEFAULT 'KTP',
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_guests_id_number UNIQUE (id_number),
    INDEX idx_guests_id_number (id_number),
    INDEX idx_guests_name (full_name),
    INDEX idx_guests_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. BOOKINGS TABLE
-- ============================================
CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(20) NOT NULL,
    guest_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    actual_check_in TIMESTAMP NULL,
    actual_check_out TIMESTAMP NULL,
    total_nights INT NOT NULL,
    room_rate DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_bookings_code UNIQUE (booking_code),
    CONSTRAINT fk_bookings_guest FOREIGN KEY (guest_id) REFERENCES guests(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_bookings_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_bookings_nights CHECK (total_nights > 0),
    CONSTRAINT chk_bookings_rate CHECK (room_rate >= 0),
    CONSTRAINT chk_bookings_amount CHECK (total_amount >= 0),
    INDEX idx_bookings_code (booking_code),
    INDEX idx_bookings_status (status),
    INDEX idx_bookings_dates (check_in_date, check_out_date),
    INDEX idx_bookings_guest (guest_id),
    INDEX idx_bookings_room (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- DEFAULT DATA - USERS
-- NOTE: Passwords are plain text for initial setup
-- Run PasswordMigrationTool after setup to hash passwords
-- Default password: admin123 for admin, staff123 for receptionist
-- ============================================
INSERT INTO users (username, password, email, full_name, role, active) VALUES
('admin', 'admin123', 'admin@hotel.com', 'Administrator', 'ADMIN', TRUE),
('receptionist', 'staff123', 'receptionist@hotel.com', 'Front Desk Staff', 'RECEPTIONIST', TRUE);

-- ============================================
-- DEFAULT DATA - ROOM TYPES
-- ============================================
INSERT INTO room_types (name, description, base_price, capacity) VALUES
('Single', 'Cozy single room with one bed, perfect for solo travelers', 350000, 1),
('Double', 'Comfortable room with double bed for couples', 500000, 2),
('Twin', 'Room with two single beds, ideal for friends or colleagues', 550000, 2),
('Deluxe', 'Spacious room with premium amenities and city view', 850000, 2),
('Suite', 'Luxury suite with separate living area and premium services', 1500000, 4),
('Family', 'Large room with multiple beds for family stays', 1200000, 5),
('Dormitory', 'Budget-friendly shared room with bunk beds', 150000, 1);

-- ============================================
-- DEFAULT DATA - ROOMS
-- ============================================
INSERT INTO rooms (room_number, room_type_id, floor, status, amenities) VALUES
-- Floor 1 - Standard rooms
('101', 1, 1, 'AVAILABLE', 'AC, TV, WiFi, Hot Water'),
('102', 1, 1, 'AVAILABLE', 'AC, TV, WiFi, Hot Water'),
('103', 2, 1, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('104', 2, 1, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('105', 7, 1, 'AVAILABLE', 'AC, Shared Bathroom, Locker'),

-- Floor 2 - Mid-range rooms
('201', 2, 2, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('202', 3, 2, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('203', 3, 2, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('204', 4, 2, 'AVAILABLE', 'AC, Smart TV, WiFi, Mini Bar, Bathtub, City View'),
('205', 4, 2, 'MAINTENANCE', 'AC, Smart TV, WiFi, Mini Bar, Bathtub, City View'),

-- Floor 3 - Premium rooms
('301', 4, 3, 'AVAILABLE', 'AC, Smart TV, WiFi, Mini Bar, Bathtub, City View'),
('302', 5, 3, 'AVAILABLE', 'AC, Smart TV, WiFi, Mini Bar, Jacuzzi, Living Room, City View'),
('303', 5, 3, 'AVAILABLE', 'AC, Smart TV, WiFi, Mini Bar, Jacuzzi, Living Room, Ocean View'),
('304', 6, 3, 'AVAILABLE', 'AC, Smart TV, WiFi, Mini Bar, Bathtub, 2 Bedrooms');

-- ============================================
-- DEFAULT DATA - SAMPLE GUESTS
-- ============================================
INSERT INTO guests (id_number, id_type, full_name, phone, email, address) VALUES
('3201234567890001', 'KTP', 'Budi Santoso', '081234567890', 'budi.santoso@email.com', 'Jl. Sudirman No. 123, Jakarta'),
('3201234567890002', 'KTP', 'Siti Rahayu', '081234567891', 'siti.rahayu@email.com', 'Jl. Gatot Subroto No. 45, Bandung'),
('A12345678', 'PASSPORT', 'John Smith', '+1234567890', 'john.smith@email.com', '123 Main Street, New York, USA'),
('3201234567890003', 'KTP', 'Ahmad Wijaya', '081234567892', 'ahmad.wijaya@email.com', 'Jl. Diponegoro No. 78, Surabaya');

-- ============================================
-- DEFAULT DATA - SAMPLE BOOKINGS
-- ============================================
INSERT INTO bookings (booking_code, guest_id, room_id, check_in_date, check_out_date, total_nights, room_rate, total_amount, status, notes, created_by) VALUES
-- Booking untuk check-in hari ini (status CONFIRMED)
('BK202512050001', 1, 3, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 2 DAY), 2, 500000, 1000000, 'CONFIRMED', 'Permintaan early check-in', 1),
-- Booking yang sudah check-in, check-out besok
('BK202512050002', 2, 6, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 3, 500000, 1500000, 'CHECKED_IN', NULL, 1),
-- Booking yang sudah check-in, check-out hari ini
('BK202512050003', 3, 7, DATE_SUB(CURDATE(), INTERVAL 3 DAY), CURDATE(), 3, 550000, 1650000, 'CHECKED_IN', 'Tamu VIP', 1),
-- Booking untuk besok
('BK202512050004', 4, 12, DATE_ADD(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), 3, 1500000, 4500000, 'CONFIRMED', 'Suite dengan pemandangan', 1);

-- Update room status sesuai booking
UPDATE rooms SET status = 'RESERVED' WHERE id = 3;  -- Booking hari ini (CONFIRMED)
UPDATE rooms SET status = 'OCCUPIED' WHERE id = 6;  -- Sudah check-in
UPDATE rooms SET status = 'OCCUPIED' WHERE id = 7;  -- Sudah check-in, check-out hari ini

-- ============================================
-- VIEWS FOR REPORTING
-- ============================================

-- View: Room availability summary
CREATE OR REPLACE VIEW v_room_summary AS
SELECT 
    rt.name AS room_type,
    COUNT(*) AS total_rooms,
    SUM(CASE WHEN r.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available,
    SUM(CASE WHEN r.status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied,
    SUM(CASE WHEN r.status = 'RESERVED' THEN 1 ELSE 0 END) AS reserved,
    SUM(CASE WHEN r.status = 'MAINTENANCE' THEN 1 ELSE 0 END) AS maintenance,
    rt.base_price
FROM rooms r
JOIN room_types rt ON r.room_type_id = rt.id
GROUP BY rt.id, rt.name, rt.base_price;

-- View: Today's check-ins
CREATE OR REPLACE VIEW v_today_checkins AS
SELECT 
    b.booking_code,
    g.full_name AS guest_name,
    g.phone AS guest_phone,
    r.room_number,
    rt.name AS room_type,
    b.check_in_date,
    b.check_out_date,
    b.total_nights,
    b.total_amount,
    b.status
FROM bookings b
JOIN guests g ON b.guest_id = g.id
JOIN rooms r ON b.room_id = r.id
JOIN room_types rt ON r.room_type_id = rt.id
WHERE b.check_in_date = CURDATE()
AND b.status IN ('PENDING', 'CONFIRMED');

-- View: Today's check-outs
CREATE OR REPLACE VIEW v_today_checkouts AS
SELECT 
    b.booking_code,
    g.full_name AS guest_name,
    g.phone AS guest_phone,
    r.room_number,
    rt.name AS room_type,
    b.check_in_date,
    b.check_out_date,
    b.total_nights,
    b.total_amount,
    b.status
FROM bookings b
JOIN guests g ON b.guest_id = g.id
JOIN rooms r ON b.room_id = r.id
JOIN room_types rt ON r.room_type_id = rt.id
WHERE b.check_out_date = CURDATE()
AND b.status = 'CHECKED_IN';

-- View: Active bookings
CREATE OR REPLACE VIEW v_active_bookings AS
SELECT 
    b.id,
    b.booking_code,
    g.id AS guest_id,
    g.full_name AS guest_name,
    g.phone AS guest_phone,
    r.id AS room_id,
    r.room_number,
    rt.name AS room_type,
    b.check_in_date,
    b.check_out_date,
    b.total_nights,
    b.room_rate,
    b.total_amount,
    b.status,
    b.notes,
    u.full_name AS created_by_name,
    b.created_at
FROM bookings b
JOIN guests g ON b.guest_id = g.id
JOIN rooms r ON b.room_id = r.id
JOIN room_types rt ON r.room_type_id = rt.id
JOIN users u ON b.created_by = u.id
WHERE b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
ORDER BY b.check_in_date;

-- ============================================
-- STORED PROCEDURES
-- ============================================

DELIMITER //

-- Procedure: Check room availability for date range
CREATE PROCEDURE sp_check_room_availability(
    IN p_room_id BIGINT,
    IN p_check_in DATE,
    IN p_check_out DATE
)
BEGIN
    SELECT COUNT(*) = 0 AS is_available
    FROM bookings
    WHERE room_id = p_room_id
    AND status NOT IN ('CANCELLED', 'CHECKED_OUT')
    AND (
        (check_in_date <= p_check_out AND check_out_date > p_check_in)
        OR (check_in_date < p_check_out AND check_out_date >= p_check_in)
        OR (check_in_date >= p_check_in AND check_out_date <= p_check_out)
    );
END //

-- Procedure: Get dashboard statistics
CREATE PROCEDURE sp_get_dashboard_stats()
BEGIN
    -- Room statistics
    SELECT 
        SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_rooms,
        SUM(CASE WHEN status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied_rooms,
        SUM(CASE WHEN status = 'RESERVED' THEN 1 ELSE 0 END) AS reserved_rooms,
        SUM(CASE WHEN status = 'MAINTENANCE' THEN 1 ELSE 0 END) AS maintenance_rooms,
        COUNT(*) AS total_rooms
    FROM rooms;
    
    -- Today's activity
    SELECT 
        (SELECT COUNT(*) FROM bookings WHERE check_in_date = CURDATE() AND status IN ('PENDING', 'CONFIRMED')) AS today_checkins,
        (SELECT COUNT(*) FROM bookings WHERE check_out_date = CURDATE() AND status = 'CHECKED_IN') AS today_checkouts;
    
    -- Monthly revenue
    SELECT COALESCE(SUM(total_amount), 0) AS monthly_revenue
    FROM bookings
    WHERE status = 'CHECKED_OUT'
    AND MONTH(actual_check_out) = MONTH(CURDATE())
    AND YEAR(actual_check_out) = YEAR(CURDATE());
END //

DELIMITER ;

-- ============================================
-- GRANT PERMISSIONS (adjust as needed)
-- ============================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON hotel_management.* TO 'hotel_app'@'localhost';
-- FLUSH PRIVILEGES;

SELECT '✓ Database schema created successfully!' AS status;
SELECT CONCAT('Total users: ', COUNT(*)) AS info FROM users;
SELECT CONCAT('Total room types: ', COUNT(*)) AS info FROM room_types;
SELECT CONCAT('Total rooms: ', COUNT(*)) AS info FROM rooms;
SELECT CONCAT('Total guests: ', COUNT(*)) AS info FROM guests;
SELECT CONCAT('Total bookings: ', COUNT(*)) AS info FROM bookings;
