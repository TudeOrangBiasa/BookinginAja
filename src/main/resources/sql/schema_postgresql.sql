-- ============================================
-- Hotel Management System - PostgreSQL Schema (Supabase)
-- Version: 1.0
-- Compatible with: PostgreSQL 15+ / Supabase
-- ============================================

-- ============================================
-- 1. USERS TABLE (Staff: Admin, Receptionist)
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'RECEPTIONIST' CHECK (role IN ('ADMIN', 'RECEPTIONIST')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- ============================================
-- 2. CUSTOMERS TABLE (Web users who book online)
-- ============================================
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email);

-- ============================================
-- 3. ROOM TYPES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS room_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    base_price DECIMAL(12,2) NOT NULL CHECK (base_price >= 0),
    capacity INT NOT NULL DEFAULT 2 CHECK (capacity > 0),
    created_at TIMESTAMPTZ DEFAULT NOW()
);


-- ============================================
-- 4. ROOMS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_type_id BIGINT NOT NULL REFERENCES room_types(id) ON DELETE RESTRICT,
    floor INT NOT NULL DEFAULT 1 CHECK (floor > 0),
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'MAINTENANCE')),
    amenities TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);
CREATE INDEX IF NOT EXISTS idx_rooms_number ON rooms(room_number);

-- ============================================
-- 5. GUESTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS guests (
    id BIGSERIAL PRIMARY KEY,
    id_number VARCHAR(50) NOT NULL UNIQUE,
    id_type VARCHAR(20) NOT NULL DEFAULT 'KTP' CHECK (id_type IN ('KTP', 'PASSPORT', 'SIM')),
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_guests_id_number ON guests(id_number);
CREATE INDEX IF NOT EXISTS idx_guests_name ON guests(full_name);

-- ============================================
-- 6. BOOKINGS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_code VARCHAR(20) NOT NULL UNIQUE,
    guest_id BIGINT NOT NULL REFERENCES guests(id) ON DELETE RESTRICT,
    room_id BIGINT NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    actual_check_in TIMESTAMPTZ,
    actual_check_out TIMESTAMPTZ,
    total_nights INT NOT NULL CHECK (total_nights > 0),
    room_rate DECIMAL(12,2) NOT NULL CHECK (room_rate >= 0),
    total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')),
    booking_source VARCHAR(10) DEFAULT 'DESKTOP' CHECK (booking_source IN ('DESKTOP', 'WEB')),
    notes TEXT,
    created_by BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    customer_id BIGINT REFERENCES customers(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT chk_booking_dates CHECK (check_out_date > check_in_date)
);

CREATE INDEX IF NOT EXISTS idx_bookings_code ON bookings(booking_code);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX IF NOT EXISTS idx_bookings_source ON bookings(booking_source);

-- ============================================
-- 7. AUTO UPDATE TIMESTAMP FUNCTION
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply triggers
DROP TRIGGER IF EXISTS trg_users_updated ON users;
CREATE TRIGGER trg_users_updated BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at();

DROP TRIGGER IF EXISTS trg_rooms_updated ON rooms;
CREATE TRIGGER trg_rooms_updated BEFORE UPDATE ON rooms FOR EACH ROW EXECUTE FUNCTION update_updated_at();

DROP TRIGGER IF EXISTS trg_guests_updated ON guests;
CREATE TRIGGER trg_guests_updated BEFORE UPDATE ON guests FOR EACH ROW EXECUTE FUNCTION update_updated_at();

DROP TRIGGER IF EXISTS trg_bookings_updated ON bookings;
CREATE TRIGGER trg_bookings_updated BEFORE UPDATE ON bookings FOR EACH ROW EXECUTE FUNCTION update_updated_at();

DROP TRIGGER IF EXISTS trg_customers_updated ON customers;
CREATE TRIGGER trg_customers_updated BEFORE UPDATE ON customers FOR EACH ROW EXECUTE FUNCTION update_updated_at();


-- ============================================
-- 8. VIEWS
-- ============================================

-- Room availability summary
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

-- Today's check-ins
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
WHERE b.check_in_date = CURRENT_DATE
AND b.status IN ('PENDING', 'CONFIRMED');

-- Today's check-outs
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
WHERE b.check_out_date = CURRENT_DATE
AND b.status = 'CHECKED_IN';

-- Web bookings view
CREATE OR REPLACE VIEW v_web_bookings AS
SELECT 
    b.id,
    b.booking_code,
    c.full_name AS customer_name,
    c.email AS customer_email,
    g.full_name AS guest_name,
    r.room_number,
    rt.name AS room_type,
    b.check_in_date,
    b.check_out_date,
    b.total_amount,
    b.status,
    b.created_at
FROM bookings b
LEFT JOIN customers c ON b.customer_id = c.id
JOIN guests g ON b.guest_id = g.id
JOIN rooms r ON b.room_id = r.id
JOIN room_types rt ON r.room_type_id = rt.id
WHERE b.booking_source = 'WEB'
ORDER BY b.created_at DESC;

-- ============================================
-- 9. FUNCTIONS
-- ============================================

-- Get available rooms for date range
CREATE OR REPLACE FUNCTION get_available_rooms(
    p_check_in DATE,
    p_check_out DATE,
    p_capacity INT DEFAULT 1
)
RETURNS TABLE (
    id BIGINT,
    room_number VARCHAR,
    room_type_id BIGINT,
    room_type VARCHAR,
    description TEXT,
    base_price DECIMAL,
    capacity INT,
    floor INT,
    amenities TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        r.id,
        r.room_number,
        rt.id AS room_type_id,
        rt.name AS room_type,
        rt.description,
        rt.base_price,
        rt.capacity,
        r.floor,
        r.amenities
    FROM rooms r
    JOIN room_types rt ON r.room_type_id = rt.id
    WHERE r.status = 'AVAILABLE'
    AND rt.capacity >= p_capacity
    AND r.id NOT IN (
        SELECT b.room_id FROM bookings b
        WHERE b.status NOT IN ('CANCELLED', 'CHECKED_OUT')
        AND b.check_in_date < p_check_out 
        AND b.check_out_date > p_check_in
    )
    ORDER BY rt.base_price, r.room_number;
END;
$$ LANGUAGE plpgsql;


-- ============================================
-- 10. DEFAULT DATA
-- ============================================

-- Users (Staff)
-- Password hashes generated with PasswordUtil (SHA-256 + salt)
-- admin123 hash: 8J+OjPCfjow=:JHzLqVnBvHxHxHxHxHxHxHxHxHxHxHxHxHxHxHxHxHw=
-- For fresh install, run PasswordMigrationTool or use plain text temporarily
INSERT INTO users (username, password, email, full_name, role, active) VALUES
('admin', 'admin123', 'admin@hotel.com', 'Administrator', 'ADMIN', TRUE),
('receptionist', 'staff123', 'receptionist@hotel.com', 'Front Desk Staff', 'RECEPTIONIST', TRUE)
ON CONFLICT (username) DO NOTHING;

-- Room Types
INSERT INTO room_types (name, description, base_price, capacity) VALUES
('Single', 'Cozy single room with one bed', 350000, 1),
('Double', 'Comfortable room with double bed', 500000, 2),
('Twin', 'Room with two single beds', 550000, 2),
('Deluxe', 'Spacious room with premium amenities', 850000, 2),
('Suite', 'Luxury suite with separate living area', 1500000, 4),
('Family', 'Large room for family stays', 1200000, 5),
('Dormitory', 'Budget-friendly shared room', 150000, 1)
ON CONFLICT (name) DO NOTHING;

-- Rooms
INSERT INTO rooms (room_number, room_type_id, floor, status, amenities) VALUES
('101', 1, 1, 'AVAILABLE', 'AC, TV, WiFi, Hot Water'),
('102', 1, 1, 'AVAILABLE', 'AC, TV, WiFi, Hot Water'),
('103', 2, 1, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('104', 2, 1, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('105', 7, 1, 'AVAILABLE', 'AC, Shared Bathroom, Locker'),
('201', 2, 2, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('202', 3, 2, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('203', 3, 2, 'AVAILABLE', 'AC, TV, WiFi, Hot Water, Mini Bar'),
('204', 4, 2, 'AVAILABLE', 'AC, Smart TV, WiFi, Mini Bar, Bathtub'),
('205', 4, 2, 'MAINTENANCE', 'AC, Smart TV, WiFi, Mini Bar, Bathtub'),
('301', 4, 3, 'AVAILABLE', 'AC, Smart TV, WiFi, Mini Bar, Bathtub'),
('302', 5, 3, 'AVAILABLE', 'AC, Smart TV, WiFi, Jacuzzi, Living Room'),
('303', 5, 3, 'AVAILABLE', 'AC, Smart TV, WiFi, Jacuzzi, Living Room'),
('304', 6, 3, 'AVAILABLE', 'AC, Smart TV, WiFi, 2 Bedrooms')
ON CONFLICT (room_number) DO NOTHING;

-- Sample Guests
INSERT INTO guests (id_number, id_type, full_name, phone, email, address) VALUES
('3201234567890001', 'KTP', 'Budi Santoso', '081234567890', 'budi@email.com', 'Jakarta'),
('3201234567890002', 'KTP', 'Siti Rahayu', '081234567891', 'siti@email.com', 'Bandung'),
('A12345678', 'PASSPORT', 'John Smith', '+1234567890', 'john@email.com', 'New York, USA'),
('3201234567890003', 'KTP', 'Ahmad Wijaya', '081234567892', 'ahmad@email.com', 'Surabaya')
ON CONFLICT (id_number) DO NOTHING;

-- ============================================
-- DONE
-- ============================================
-- Run this in Supabase SQL Editor
