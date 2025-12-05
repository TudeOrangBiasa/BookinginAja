package com.example.opp.service;

import com.example.opp.model.Guest;
import com.example.opp.repository.GuestRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService() {
        this.guestRepository = new GuestRepository();
    }

    public List<Guest> getAllGuests() throws SQLException {
        return guestRepository.findAll();
    }

    public Optional<Guest> getGuestById(Long id) throws SQLException {
        return guestRepository.findById(id);
    }

    public Optional<Guest> getGuestByIdNumber(String idNumber) throws SQLException {
        return guestRepository.findByIdNumber(idNumber);
    }

    public List<Guest> searchGuests(String name) throws SQLException {
        return guestRepository.searchByName(name);
    }

    public Guest createOrGetGuest(String idNumber, String fullName, String phone, String email) throws SQLException {
        Optional<Guest> existing = guestRepository.findByIdNumber(idNumber);
        if (existing.isPresent()) {
            return existing.get();
        }

        Guest guest = new Guest(idNumber, fullName, phone);
        guest.setEmail(email);
        long id = guestRepository.save(guest);
        guest.setId(id);
        return guest;
    }

    public long createGuest(Guest guest) throws SQLException {
        if (guestRepository.existsByIdNumber(guest.getIdNumber())) {
            throw new IllegalStateException("Guest with this ID number already exists");
        }
        return guestRepository.save(guest);
    }

    public boolean updateGuest(Guest guest) throws SQLException {
        return guestRepository.update(guest) > 0;
    }

    public boolean deleteGuest(Long id) throws SQLException {
        return guestRepository.delete(id) > 0;
    }
}
