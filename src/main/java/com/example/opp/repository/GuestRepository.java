package com.example.opp.repository;

import com.example.opp.model.Guest;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class GuestRepository extends BaseRepository<Guest, Long> {

    @Override
    protected String getTableName() { return "guests"; }

    @Override
    protected Guest mapRow(ResultSet rs) throws SQLException {
        Guest guest = new Guest();
        guest.setId(rs.getLong("id"));
        guest.setIdNumber(rs.getString("id_number"));
        guest.setIdType(Guest.IdType.valueOf(rs.getString("id_type")));
        guest.setFullName(rs.getString("full_name"));
        guest.setPhone(rs.getString("phone"));
        guest.setEmail(rs.getString("email"));
        guest.setAddress(rs.getString("address"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) guest.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) guest.setUpdatedAt(updatedAt.toLocalDateTime());

        return guest;
    }

    public Optional<Guest> findById(Long id) throws SQLException {
        return super.findById("id", id);
    }

    public Optional<Guest> findByIdNumber(String idNumber) throws SQLException {
        return queryOne("SELECT * FROM guests WHERE id_number = ?", idNumber);
    }

    public List<Guest> searchByName(String name) throws SQLException {
        return query("SELECT * FROM guests WHERE full_name LIKE ? ORDER BY full_name", "%" + name + "%");
    }

    public long save(Guest guest) throws SQLException {
        return insert(
            "INSERT INTO guests (id_number, id_type, full_name, phone, email, address) VALUES (?, ?, ?, ?, ?, ?)",
            guest.getIdNumber(),
            guest.getIdType().name(),
            guest.getFullName(),
            guest.getPhone(),
            guest.getEmail(),
            guest.getAddress()
        );
    }

    public int update(Guest guest) throws SQLException {
        return execute(
            "UPDATE guests SET id_number = ?, id_type = ?, full_name = ?, phone = ?, email = ?, address = ? WHERE id = ?",
            guest.getIdNumber(),
            guest.getIdType().name(),
            guest.getFullName(),
            guest.getPhone(),
            guest.getEmail(),
            guest.getAddress(),
            guest.getId()
        );
    }

    public int delete(Long id) throws SQLException {
        return execute("DELETE FROM guests WHERE id = ?", id);
    }

    public boolean existsByIdNumber(String idNumber) throws SQLException {
        return findByIdNumber(idNumber).isPresent();
    }

    public List<Guest> findAll() throws SQLException {
        return super.findAll();
    }
}
