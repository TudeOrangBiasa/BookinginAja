# Supabase Setup Guide

## 1. Buat Project di Supabase

1. Buka https://supabase.com
2. Sign up / Login
3. Klik **New Project**
4. Isi:
   - Name: `hotel-management`
   - Database Password: (simpan password ini!)
   - Region: Singapore (terdekat)
5. Tunggu project selesai dibuat (~2 menit)

## 2. Dapatkan Connection Info

1. Buka project → **Settings** → **Database**
2. Scroll ke **Connection string** → pilih **JDBC**
3. Catat:
   - Host: `db.xxxxx.supabase.co`
   - Port: `5432`
   - Database: `postgres`
   - User: `postgres`
   - Password: (yang kamu buat tadi)

## 3. Update config.properties

Edit file `src/main/resources/config.properties`:

```properties
db.host=db.YOUR_PROJECT_REF.supabase.co
db.port=5432
db.name=postgres
db.username=postgres
db.password=YOUR_DATABASE_PASSWORD
```

## 4. Jalankan Schema SQL

1. Buka Supabase Dashboard → **SQL Editor**
2. Klik **New Query**
3. Copy-paste isi file `src/main/resources/sql/schema_postgresql.sql`
4. Klik **Run**

## 5. Test Koneksi

Jalankan aplikasi JavaFX:

```bash
mvn clean javafx:run
```

## Troubleshooting

### Connection Refused
- Pastikan IP kamu tidak di-block
- Buka **Settings** → **Database** → **Connection Pooling** → Enable

### SSL Error
- Pastikan JDBC URL ada `?sslmode=require`

### Timeout
- Supabase free tier ada limit koneksi
- Kurangi `db.pool.size` ke 3-5

## Security Best Practice

1. **Jangan commit password ke Git!**
   - Tambahkan `config.properties` ke `.gitignore`
   - Atau gunakan environment variables

2. **Gunakan Connection Pooling**
   - Supabase: Settings → Database → Connection Pooling
   - Ganti port ke `6543` untuk pooler

3. **Row Level Security (RLS)**
   - Untuk web booking, enable RLS di Supabase
   - Buat policies untuk customers table
