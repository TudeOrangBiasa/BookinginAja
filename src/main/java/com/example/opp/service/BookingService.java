package com.example.opp.service;

import com.example.opp.model.*;
import com.example.opp.repository.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final RoomTypeRepository roomTypeRepository;

    public BookingService() {
        this.bookingRepository = new BookingRepository();
        this.roomRepository = new RoomRepository();
        this.guestRepository = new GuestRepository();
        this.roomTypeRepository = new RoomTypeRepository();
    }

    public Booking createBooking(Long guestId, Long roomId, LocalDate checkIn, LocalDate checkOut, Long createdBy) throws SQLException {
        if (!bookingRepository.isRoomAvailable(roomId, checkIn, checkOut)) {
            throw new IllegalStateException("Room is not available for selected dates");
        }

        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        RoomType roomType = roomTypeRepository.findById(room.getRoomTypeId())
            .orElseThrow(() -> new IllegalArgumentException("Room type not found"));

        Booking booking = new Booking();
        booking.setBookingCode(generateBookingCode());
        booking.setGuestId(guestId);
        booking.setRoomId(roomId);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        booking.setRoomRate(roomType.getBasePrice());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedBy(createdBy);
        booking.calculateTotals();

        long id = bookingRepository.save(booking);
        booking.setId(id);

        if (checkIn.equals(LocalDate.now())) {
            roomRepository.updateStatus(roomId, RoomStatus.RESERVED);
        }

        return booking;
    }

    public boolean checkIn(Long bookingId) throws SQLException {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) return false;

        Booking booking = bookingOpt.get();
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PENDING) {
            return false;
        }

        bookingRepository.checkIn(bookingId);
        roomRepository.updateStatus(booking.getRoomId(), RoomStatus.OCCUPIED);
        return true;
    }

    public boolean checkOut(Long bookingId) throws SQLException {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) return false;

        Booking booking = bookingOpt.get();
        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            return false;
        }

        bookingRepository.checkOut(bookingId);
        roomRepository.updateStatus(booking.getRoomId(), RoomStatus.AVAILABLE);
        return true;
    }

    public boolean cancelBooking(Long bookingId) throws SQLException {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) return false;

        Booking booking = bookingOpt.get();
        if (booking.getStatus() == BookingStatus.CHECKED_OUT || booking.getStatus() == BookingStatus.CANCELLED) {
            return false;
        }

        bookingRepository.updateStatus(bookingId, BookingStatus.CANCELLED);

        if (booking.getStatus() == BookingStatus.CHECKED_IN) {
            roomRepository.updateStatus(booking.getRoomId(), RoomStatus.AVAILABLE);
        }

        return true;
    }

    public List<Booking> getActiveBookings() throws SQLException {
        List<Booking> bookings = bookingRepository.findActiveBookings();
        for (Booking booking : bookings) {
            loadRelations(booking);
        }
        return bookings;
    }

    public List<Booking> getTodayCheckIns() throws SQLException {
        List<Booking> bookings = bookingRepository.findTodayCheckIns();
        for (Booking booking : bookings) {
            loadRelations(booking);
        }
        return bookings;
    }

    public List<Booking> getTodayCheckOuts() throws SQLException {
        List<Booking> bookings = bookingRepository.findTodayCheckOuts();
        for (Booking booking : bookings) {
            loadRelations(booking);
        }
        return bookings;
    }

    public Optional<Booking> getBookingById(Long id) throws SQLException {
        Optional<Booking> booking = bookingRepository.findById(id);
        booking.ifPresent(this::loadRelations);
        return booking;
    }

    public Optional<Booking> getBookingByCode(String code) throws SQLException {
        Optional<Booking> booking = bookingRepository.findByBookingCode(code);
        booking.ifPresent(this::loadRelations);
        return booking;
    }

    public BigDecimal calculateTotal(Long roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        RoomType roomType = roomTypeRepository.findById(room.getRoomTypeId())
            .orElseThrow(() -> new IllegalArgumentException("Room type not found"));

        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        return roomType.getBasePrice().multiply(BigDecimal.valueOf(nights));
    }

    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        return bookingRepository.isRoomAvailable(roomId, checkIn, checkOut);
    }

    private String generateBookingCode() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "BK" + date + random;
    }

    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = bookingRepository.findAll();
        for (Booking booking : bookings) {
            loadRelations(booking);
        }
        return bookings;
    }

    private void loadRelations(Booking booking) {
        try {
            guestRepository.findById(booking.getGuestId()).ifPresent(booking::setGuest);
            roomRepository.findById(booking.getRoomId()).ifPresent(room -> {
                booking.setRoom(room);
                try {
                    roomTypeRepository.findById(room.getRoomTypeId()).ifPresent(room::setRoomType);
                } catch (SQLException e) {
                    // Room type is optional for display
                }
            });
        } catch (SQLException e) {
            // Relations are optional for display
        }
    }
}
