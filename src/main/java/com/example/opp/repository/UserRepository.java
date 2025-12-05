package com.example.opp.repository;

import com.example.opp.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public class UserRepository extends BaseRepository<User, Long> {

    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setActive(rs.getBoolean("active"));

        String roleStr = rs.getString("role");
        if (roleStr != null) {
            user.setRole(com.example.opp.model.Role.valueOf(roleStr));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) user.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) user.setUpdatedAt(updatedAt.toLocalDateTime());

        return user;
    }

    public Optional<User> findById(Long id) throws SQLException {
        return super.findById("id", id);
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        return queryOne("SELECT * FROM users WHERE username = ?", username);
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        return queryOne("SELECT * FROM users WHERE email = ?", email);
    }

    public List<User> findAllActive() throws SQLException {
        return query("SELECT * FROM users WHERE active = true");
    }

    public long save(User user) throws SQLException {
        return insert(
            "INSERT INTO users (username, password, email, full_name, role, active) VALUES (?, ?, ?, ?, ?, ?)",
            user.getUsername(),
            user.getPassword(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.isActive()
        );
    }

    public int update(User user) throws SQLException {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            return execute(
                "UPDATE users SET email = ?, full_name = ?, role = ?, active = ?, password = ?, updated_at = NOW() WHERE id = ?",
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.isActive(),
                user.getPassword(),
                user.getId()
            );
        }
        return execute(
            "UPDATE users SET email = ?, full_name = ?, role = ?, active = ?, updated_at = NOW() WHERE id = ?",
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.isActive(),
            user.getId()
        );
    }

    public List<User> findAll() throws SQLException {
        return query("SELECT * FROM users ORDER BY created_at DESC");
    }

    public int updatePassword(Long id, String newPassword) throws SQLException {
        return execute("UPDATE users SET password = ?, updated_at = NOW() WHERE id = ?", newPassword, id);
    }

    public int delete(Long id) throws SQLException {
        return execute("DELETE FROM users WHERE id = ?", id);
    }

    public boolean existsByUsername(String username) throws SQLException {
        return findByUsername(username).isPresent();
    }
}
