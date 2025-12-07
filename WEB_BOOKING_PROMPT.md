# Context: Hotel Management System - Web Booking

## Overview

Saya punya aplikasi Hotel Management System (JavaFX Desktop) yang sudah terkoneksi ke **Supabase PostgreSQL**. Sekarang saya mau buat versi web untuk customer booking online (seperti Traveloka).

Gunakan context ini sebagai referensi. Jangan generate code kecuali saya minta.

## Database Info

- **Host**: `aws-1-ap-southeast-1.pooler.supabase.com`
- **Port**: `5432`
- **Database**: `postgres`
- **Username**: `postgres.snbcakrfmmdajtwrldle`

## Existing Tables (PostgreSQL/Supabase)

```sql
-- users (untuk staff hotel - JANGAN DIPAKAI di web)
-- customers (untuk user web yang booking)
-- room_types (tipe kamar: Single, Double, Suite, dll)
-- rooms (kamar dengan status: AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE)
-- guests (data tamu yang menginap)
-- bookings (reservasi dengan status: PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED)
```

## Schema Detail

```sql
-- CUSTOMERS (Web users)
CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ROOM_TYPES
CREATE TABLE room_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    base_price DECIMAL(12,2) NOT NULL,
    capacity INT NOT NULL DEFAULT 2
);

-- ROOMS
CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE,
    room_type_id BIGINT REFERENCES room_types(id),
    floor INT NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE', -- AVAILABLE, OCCUPIED, RESERVED, MAINTENANCE
    amenities TEXT
);

-- GUESTS
CREATE TABLE guests (
    id BIGSERIAL PRIMARY KEY,
    id_number VARCHAR(50) NOT NULL UNIQUE,
    id_type VARCHAR(20) DEFAULT 'KTP', -- KTP, PASSPORT, SIM
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT
);

-- BOOKINGS
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    booking_code VARCHAR(20) NOT NULL UNIQUE,
    guest_id BIGINT REFERENCES guests(id),
    room_id BIGINT REFERENCES rooms(id),
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    total_nights INT NOT NULL,
    room_rate DECIMAL(12,2) NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    booking_source VARCHAR(10) DEFAULT 'WEB', -- DESKTOP, WEB
    customer_id BIGINT REFERENCES customers(id),
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
```

## Function untuk Cek Ketersediaan Kamar

```sql
-- Sudah ada di database
SELECT * FROM get_available_rooms('2025-12-10', '2025-12-12', 2);
```

## Requirements untuk Web App

### Tech Stack
- **Frontend**: Next.js 14 (App Router) + Tailwind CSS
- **Backend**: Supabase Client (langsung dari Next.js, atau bisa pakai API routes)
- **Auth**: Supabase Auth atau custom JWT
- **Database**: Supabase PostgreSQL (sudah ada)

### Halaman yang Dibutuhkan

1. **Landing Page** (`/`)
   - Hero section dengan search form (check-in, check-out, jumlah tamu)
   - Featured rooms
   - Hotel info

2. **Search Results** (`/rooms?checkin=...&checkout=...&guests=...`)
   - List kamar yang tersedia untuk tanggal tersebut
   - Filter by room type, price range
   - Sort by price

3. **Room Detail** (`/rooms/[id]`)
   - Foto kamar, amenities, description
   - Harga per malam
   - Button "Book Now"

4. **Auth Pages**
   - `/login` - Login customer
   - `/register` - Register customer baru

5. **Booking Flow** (`/booking/[roomId]`)
   - Form isi data tamu (nama, KTP/passport, phone, email)
   - Ringkasan booking (tanggal, kamar, total harga)
   - Konfirmasi booking

6. **Booking Success** (`/booking/success/[bookingCode]`)
   - Tampilkan booking code
   - Detail booking
   - Instruksi selanjutnya

7. **My Bookings** (`/my-bookings`)
   - List booking customer yang login
   - Status masing-masing booking

8. **Booking Detail** (`/my-bookings/[bookingCode]`)
   - Detail lengkap booking
   - Option cancel (jika status masih PENDING)

### Business Logic

1. **Booking Flow**:
   - Customer pilih tanggal → cari kamar available
   - Pilih kamar → login/register
   - Isi data tamu → konfirmasi
   - Booking tersimpan dengan status `PENDING`, source `WEB`
   - Staff hotel konfirmasi via Desktop app → status jadi `CONFIRMED`

2. **Booking Code Format**: `BK` + `YYYYMMDD` + `XXXX` (contoh: BK202512100001)

3. **Harga**: Ambil dari `room_types.base_price` × jumlah malam

4. **Validasi**:
   - Check-out harus setelah check-in
   - Minimal 1 malam
   - Kamar harus available untuk tanggal tersebut

### Password Hashing (PENTING!)

Gunakan format yang sama dengan Desktop app:
```javascript
// SHA-256 dengan salt (16 bytes)
// Format: base64(salt):base64(hash)

import crypto from 'crypto';

function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const hash = crypto.createHash('sha256').update(salt).update(password).digest();
  return `${salt.toString('base64')}:${hash.toString('base64')}`;
}

function verifyPassword(password, storedHash) {
  const [saltB64, hashB64] = storedHash.split(':');
  const salt = Buffer.from(saltB64, 'base64');
  const expectedHash = Buffer.from(hashB64, 'base64');
  const actualHash = crypto.createHash('sha256').update(salt).update(password).digest();
  return crypto.timingSafeEqual(expectedHash, actualHash);
}
```

### UI/UX Guidelines

- Mobile-first responsive design
- Clean, modern look (seperti Traveloka/Agoda)
- Loading states untuk semua async operations
- Error handling yang user-friendly
- Bahasa Indonesia

### Folder Structure

```
hotel-web/
├── app/
│   ├── page.tsx                 # Landing
│   ├── rooms/
│   │   ├── page.tsx             # Search results
│   │   └── [id]/page.tsx        # Room detail
│   ├── booking/
│   │   ├── [roomId]/page.tsx    # Booking form
│   │   └── success/[code]/page.tsx
│   ├── my-bookings/
│   │   ├── page.tsx             # List bookings
│   │   └── [code]/page.tsx      # Booking detail
│   ├── login/page.tsx
│   ├── register/page.tsx
│   └── layout.tsx
├── components/
│   ├── ui/                      # Reusable UI components
│   ├── RoomCard.tsx
│   ├── BookingForm.tsx
│   ├── SearchForm.tsx
│   └── Navbar.tsx
├── lib/
│   ├── supabase.ts              # Supabase client
│   ├── auth.ts                  # Auth helpers
│   └── utils.ts
├── types/
│   └── index.ts                 # TypeScript types
└── .env.local                   # Supabase credentials
```

## Supabase Credentials (untuk .env.local)

```
NEXT_PUBLIC_SUPABASE_URL=https://snbcakrfmmdajtwrldle.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=[GET FROM SUPABASE DASHBOARD > Settings > API]
SUPABASE_SERVICE_ROLE_KEY=[GET FROM SUPABASE DASHBOARD > Settings > API]
```
