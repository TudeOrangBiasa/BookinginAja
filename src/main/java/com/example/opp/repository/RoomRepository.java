package com.example.opp.repository;

import com.example.opp.model.Room;
import com.example.opp.model.RoomStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class RoomRepository extends BaseRepository<Room, Long> {

    @Override
    protected String getTableName() { return "rooms"; }

    @Override
    protected Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getLong("id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomTypeId(rs.getLong("room_type_id"));
        room.setFloor(rs.getInt("floor"));
        room.setStatus(RoomStatus.valueOf(rs.getString("status")));
        room.setAmenities(rs.getString("amenities"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) room.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) room.setUpdatedAt(updatedAt.toLocalDateTime());

        return room;
    }

    public Optional<Room> findById(Long id) throws SQLException {
        return super.findById("id", id);
    }

    public Optional<Room> findByRoomNumber(String roomNumber) throws SQLException {
        return queryOne("SELECT * FROM rooms WHERE room_number = ?", roomNumber);
    }

    public List<Room> findByStatus(RoomStatus status) throws SQLException {
        return query("SELECT * FROM rooms WHERE status = ?", status.name());
    }

    public List<Room> findAvailable() throws SQLException {
        return findByStatus(RoomStatus.AVAILABLE);
    }

    public List<Room> findByFloor(int floor) throws SQLException {
        return query("SELECT * FROM rooms WHERE floor = ? ORDER BY room_number", floor);
    }

    public long save(Room room) throws SQLException {
        return insert(
            "INSERT INTO rooms (room_number, room_type_id, floor, status, amenities) VALUES (?, ?, ?, ?, ?)",
            room.getRoomNumber(),
            room.getRoomTypeId(),
            room.getFloor(),
            room.getStatus().name(),
            room.getAmenities()
        );
    }

    public int updateStatus(Long id, RoomStatus status) throws SQLException {
        return execute("UPDATE rooms SET status = ? WHERE id = ?", status.name(), id);
    }

    public int update(Room room) throws SQLException {
        return execute(
            "UPDATE rooms SET room_number = ?, room_type_id = ?, floor = ?, status = ?, amenities = ? WHERE id = ?",
            room.getRoomNumber(),
            room.getRoomTypeId(),
            room.getFloor(),
            room.getStatus().name(),
            room.getAmenities(),
            room.getId()
        );
    }

    public int delete(Long id) throws SQLException {
        return execute("DELETE FROM rooms WHERE id = ?", id);
    }

    public int countByStatus(RoomStatus status) throws SQLException {
        return db.executeWithConnection(conn -> {
            var stmt = conn.prepareStatement("SELECT COUNT(*) FROM rooms WHERE status = ?");
            stmt.setString(1, status.name());
            var rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        });
    }

    public List<Room> findAll() throws SQLException {
        return super.findAll();
    }
}
