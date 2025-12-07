package com.example.opp.repository;

import com.example.opp.model.RoomType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class RoomTypeRepository extends BaseRepository<RoomType, Long> {

    @Override
    protected String getTableName() { return "room_types"; }

    @Override
    protected RoomType mapRow(ResultSet rs) throws SQLException {
        RoomType type = new RoomType();
        type.setId(rs.getLong("id"));
        type.setName(rs.getString("name"));
        type.setDescription(rs.getString("description"));
        type.setBasePrice(rs.getBigDecimal("base_price"));
        type.setCapacity(rs.getInt("capacity"));
        type.setImageUrl(rs.getString("image_url"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) type.setCreatedAt(createdAt.toLocalDateTime());

        return type;
    }

    public Optional<RoomType> findById(Long id) throws SQLException {
        return super.findById("id", id);
    }

    public Optional<RoomType> findByName(String name) throws SQLException {
        return queryOne("SELECT * FROM room_types WHERE name = ?", name);
    }

    public long save(RoomType type) throws SQLException {
        return insert(
            "INSERT INTO room_types (name, description, base_price, capacity) VALUES (?, ?, ?, ?)",
            type.getName(),
            type.getDescription(),
            type.getBasePrice(),
            type.getCapacity()
        );
    }

    public int update(RoomType type) throws SQLException {
        return execute(
            "UPDATE room_types SET name = ?, description = ?, base_price = ?, capacity = ? WHERE id = ?",
            type.getName(),
            type.getDescription(),
            type.getBasePrice(),
            type.getCapacity(),
            type.getId()
        );
    }

    public int delete(Long id) throws SQLException {
        return execute("DELETE FROM room_types WHERE id = ?", id);
    }

    public java.util.List<RoomType> findAll() throws SQLException {
        return super.findAll();
    }
}
