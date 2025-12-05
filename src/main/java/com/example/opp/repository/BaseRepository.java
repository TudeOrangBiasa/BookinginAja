package com.example.opp.repository;

import com.example.opp.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class BaseRepository<T, ID> {

    protected final DatabaseManager db = DatabaseManager.getInstance();

    protected abstract T mapRow(ResultSet rs) throws SQLException;
    protected abstract String getTableName();

    protected Optional<T> findById(String idColumn, ID id) throws SQLException {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + idColumn + " = ?";
        return db.executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, id);
                ResultSet rs = stmt.executeQuery();
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        });
    }

    protected List<T> findAll() throws SQLException {
        String sql = "SELECT * FROM " + getTableName();
        return db.executeWithConnection(conn -> {
            List<T> results = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        });
    }

    protected List<T> query(String sql, Object... params) throws SQLException {
        return db.executeWithConnection(conn -> {
            List<T> results = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
            return results;
        });
    }

    protected Optional<T> queryOne(String sql, Object... params) throws SQLException {
        return db.executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                ResultSet rs = stmt.executeQuery();
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        });
    }

    protected int execute(String sql, Object... params) throws SQLException {
        return db.executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                setParameters(stmt, params);
                return stmt.executeUpdate();
            }
        });
    }

    protected long insert(String sql, Object... params) throws SQLException {
        return db.executeWithConnection(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setParameters(stmt, params);
                stmt.executeUpdate();
                ResultSet keys = stmt.getGeneratedKeys();
                return keys.next() ? keys.getLong(1) : -1;
            }
        });
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}
