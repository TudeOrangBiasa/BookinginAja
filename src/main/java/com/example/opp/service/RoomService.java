package com.example.opp.service;

import com.example.opp.model.Room;
import com.example.opp.model.RoomStatus;
import com.example.opp.model.RoomType;
import com.example.opp.repository.RoomRepository;
import com.example.opp.repository.RoomTypeRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    public RoomService() {
        this.roomRepository = new RoomRepository();
        this.roomTypeRepository = new RoomTypeRepository();
    }

    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = roomRepository.findAll();
        for (Room room : rooms) {
            loadRoomType(room);
        }
        return rooms;
    }

    public List<Room> getAvailableRooms() throws SQLException {
        List<Room> rooms = roomRepository.findAvailable();
        for (Room room : rooms) {
            loadRoomType(room);
        }
        return rooms;
    }

    public Optional<Room> getRoomById(Long id) throws SQLException {
        Optional<Room> room = roomRepository.findById(id);
        room.ifPresent(this::loadRoomType);
        return room;
    }

    public Optional<Room> getRoomByNumber(String roomNumber) throws SQLException {
        Optional<Room> room = roomRepository.findByRoomNumber(roomNumber);
        room.ifPresent(this::loadRoomType);
        return room;
    }

    public long createRoom(Room room) throws SQLException {
        return roomRepository.save(room);
    }

    public boolean updateRoom(Room room) throws SQLException {
        return roomRepository.update(room) > 0;
    }

    public boolean updateRoomStatus(Long roomId, RoomStatus status) throws SQLException {
        return roomRepository.updateStatus(roomId, status) > 0;
    }

    public boolean deleteRoom(Long id) throws SQLException {
        return roomRepository.delete(id) > 0;
    }

    public List<RoomType> getAllRoomTypes() throws SQLException {
        return roomTypeRepository.findAll();
    }

    public int getAvailableCount() throws SQLException {
        return roomRepository.countByStatus(RoomStatus.AVAILABLE);
    }

    public int getOccupiedCount() throws SQLException {
        return roomRepository.countByStatus(RoomStatus.OCCUPIED);
    }

    public int getReservedCount() throws SQLException {
        return roomRepository.countByStatus(RoomStatus.RESERVED);
    }

    public int getMaintenanceCount() throws SQLException {
        return roomRepository.countByStatus(RoomStatus.MAINTENANCE);
    }

    public int getTotalRoomCount() throws SQLException {
        return roomRepository.findAll().size();
    }

    private void loadRoomType(Room room) {
        try {
            roomTypeRepository.findById(room.getRoomTypeId())
                .ifPresent(room::setRoomType);
        } catch (SQLException e) {
            // Log silently - room type is optional for display
        }
    }
}
