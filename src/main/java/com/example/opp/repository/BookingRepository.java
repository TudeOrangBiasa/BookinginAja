package com.example.opp.repository;

import com.example.opp.model.Booking;
import com.example.opp.model.BookingStatus;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class BookingRepository extends BaseRepository<Booking, Long> {

    @Override
    protected String getTableName() { return "bookings"; }

    @Override
    protected Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getLong("id"));
        booking.setBookingCode(rs.getString("booking_code"));
        booking.setGuestId(rs.getLong("guest_id"));
        booking.setRoomId(rs.getLong("room_id"));
        booking.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        booking.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());

        Timestamp actualIn = rs.getTimestamp("actual_check_in");
        if (actualIn != null) booking.setActualCheckIn(actualIn.toLocalDateTime());

        Timestamp actualOut = rs.getTimestamp("actual_check_out");
        if (actualOut != null) booking.setActualCheckOut(actualOut.toLocalDateTime());

        booking.setTotalNights(rs.getInt("total_nights"));
        booking.setRoomRate(rs.getBigDecimal("room_rate"));
        booking.setTotalAmount(rs.getBigDecimal("total_amount"));
        booking.setStatus(BookingStatus.valueOf(rs.getString("status")));
        booking.setNotes(rs.getString("notes"));
        booking.setCreatedBy(rs.getLong("created_by"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) booking.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) booking.setUpdatedAt(updatedAt.toLocalDateTime());

        return booking;
    }

    public Optional<Booking> findById(Long id) throws SQLException {
        return super.findById("id", id);
    }

    public Optional<Booking> findByBookingCode(String code) throws SQLException {
        return queryOne("SELECT * FROM bookings WHERE booking_code = ?", code);
    }


    public List<Booking> findByStatus(BookingStatus status) throws SQLException {
        return query("SELECT * FROM bookings WHERE status = ? ORDER BY check_in_date", status.name());
    }

    public List<Booking> findByGuestId(Long guestId) throws SQLException {
        return query("SELECT * FROM bookings WHERE guest_id = ? ORDER BY created_at DESC", guestId);
    }

    public List<Booking> findTodayCheckIns() throws SQLException {
        return query(
            "SELECT * FROM bookings WHERE check_in_date = ? AND status IN ('PENDING', 'CONFIRMED') ORDER BY created_at",
            Date.valueOf(LocalDate.now())
        );
    }

    public List<Booking> findTodayCheckOuts() throws SQLException {
        return query(
            "SELECT * FROM bookings WHERE check_out_date = ? AND status = 'CHECKED_IN' ORDER BY created_at",
            Date.valueOf(LocalDate.now())
        );
    }

    public List<Booking> findActiveBookings() throws SQLException {
        return query("SELECT * FROM bookings WHERE status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN') ORDER BY check_in_date");
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        return db.executeWithConnection(conn -> {
            var stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND status NOT IN ('CANCELLED', 'CHECKED_OUT') " +
                "AND ((check_in_date <= ? AND check_out_date > ?) OR (check_in_date < ? AND check_out_date >= ?) " +
                "OR (check_in_date >= ? AND check_out_date <= ?))"
            );
            stmt.setLong(1, roomId);
            stmt.setDate(2, Date.valueOf(checkOut));
            stmt.setDate(3, Date.valueOf(checkIn));
            stmt.setDate(4, Date.valueOf(checkOut));
            stmt.setDate(5, Date.valueOf(checkIn));
            stmt.setDate(6, Date.valueOf(checkIn));
            stmt.setDate(7, Date.valueOf(checkOut));
            var rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) == 0;
        });
    }

    public long save(Booking booking) throws SQLException {
        return insert(
            "INSERT INTO bookings (booking_code, guest_id, room_id, check_in_date, check_out_date, " +
            "total_nights, room_rate, total_amount, status, notes, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
            booking.getBookingCode(),
            booking.getGuestId(),
            booking.getRoomId(),
            Date.valueOf(booking.getCheckInDate()),
            Date.valueOf(booking.getCheckOutDate()),
            booking.getTotalNights(),
            booking.getRoomRate(),
            booking.getTotalAmount(),
            booking.getStatus().name(),
            booking.getNotes(),
            booking.getCreatedBy()
        );
    }

    public int updateStatus(Long id, BookingStatus status) throws SQLException {
        return execute("UPDATE bookings SET status = ? WHERE id = ?", status.name(), id);
    }

    public int checkIn(Long id) throws SQLException {
        return execute(
            "UPDATE bookings SET status = 'CHECKED_IN', actual_check_in = NOW() WHERE id = ?", id
        );
    }

    public int checkOut(Long id) throws SQLException {
        return execute(
            "UPDATE bookings SET status = 'CHECKED_OUT', actual_check_out = NOW() WHERE id = ?", id
        );
    }

    public int delete(Long id) throws SQLException {
        return execute("DELETE FROM bookings WHERE id = ?", id);
    }

    public List<Booking> findAll() throws SQLException {
        return query("SELECT * FROM bookings ORDER BY created_at DESC");
    }

    public int countNewWebBookingsSince(java.time.LocalDateTime since) throws SQLException {
        return db.executeWithConnection(conn -> {
            var stmt = conn.prepareStatement("SELECT COUNT(*) FROM bookings WHERE booking_source = 'WEB' AND created_at > ?");
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(since));
            var rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        });
    }

    public List<Booking> findWebBookingsSince(java.time.LocalDateTime since) throws SQLException {
        return query("SELECT * FROM bookings WHERE booking_source = 'WEB' AND created_at > ? ORDER BY created_at DESC", 
                    java.sql.Timestamp.valueOf(since));
    }
}