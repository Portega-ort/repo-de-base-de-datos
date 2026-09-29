package com.retfinalo.repository;

import com.retfinalo.config.DatabaseConnection;
import com.retfinalo.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UsuarioRepository {
    private static final String SELECT_COLUMNS = "id_usuario, nombre, apellido, email, password_hash, fecha_registro, es_admin";

    private static final String FIND_BY_EMAIL =
            "SELECT " + SELECT_COLUMNS + " FROM usuarios WHERE email = ?";

    private static final String FIND_BY_ID =
            "SELECT " + SELECT_COLUMNS + " FROM usuarios WHERE id_usuario = ?";

    private static final String INSERT = """
            INSERT INTO usuarios (nombre, apellido, email, password_hash) VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_PROFILE = """
            UPDATE usuarios SET nombre = ?, apellido = ?, email = ? WHERE id_usuario = ?
            """;

    private static final String UPDATE_PASSWORD = """
            UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?
            """;

    public Optional<Usuario> findByEmail(String email) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public Optional<Usuario> findById(long id) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public long insert(String nombre, String apellido, String email, String passwordHash) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, email);
            statement.setString(4, passwordHash);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new SQLException("No se obtuvo el id del usuario");
            }
        }
    }

    public boolean updateProfile(long id, String nombre, String apellido, String email) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PROFILE)) {
            statement.setString(1, nombre);
            statement.setString(2, apellido);
            statement.setString(3, email);
            statement.setLong(4, id);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(long id, String passwordHash) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD)) {
            statement.setString(1, passwordHash);
            statement.setLong(2, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Usuario map(ResultSet result) throws SQLException {
        return new Usuario(
                result.getLong("id_usuario"),
                result.getString("nombre"),
                result.getString("apellido"),
                result.getString("email"),
                result.getString("password_hash"),
                result.getTimestamp("fecha_registro").toLocalDateTime().toString().replace('T', ' '),
                result.getBoolean("es_admin"));
    }
}