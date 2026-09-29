package com.retfinalo.repository;

import com.retfinalo.config.DatabaseConnection;
import com.retfinalo.model.Habito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HabitoRepository {
    private static final String SELECT_COLUMNS = """
            h.id_habito, h.id_usuario, h.id_categoria, h.nombre, h.descripcion,
            c.nombre AS categoria, h.tipo_meta, h.meta_diaria, h.unidad_medida, h.activo, h.fecha_creacion
            """;

    private static final String FIND_BY_USUARIO =
            "SELECT " + SELECT_COLUMNS + " FROM habitos h JOIN categorias c ON c.id_categoria = h.id_categoria " +
            "WHERE h.id_usuario = ? AND h.activo = TRUE ORDER BY h.fecha_creacion DESC, h.nombre";

    private static final String FIND_BY_ID_AND_USUARIO =
            "SELECT " + SELECT_COLUMNS + " FROM habitos h JOIN categorias c ON c.id_categoria = h.id_categoria " +
            "WHERE h.id_habito = ? AND h.id_usuario = ?";

    private static final String INSERT = """
            INSERT INTO habitos (id_usuario, id_categoria, nombre, descripcion, tipo_meta, meta_diaria, unidad_medida, activo, fecha_creacion)
            VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, ?)
            """;

    private static final String UPDATE = """
            UPDATE habitos
            SET id_categoria = ?, nombre = ?, descripcion = ?, tipo_meta = ?, meta_diaria = ?, unidad_medida = ?
            WHERE id_habito = ? AND id_usuario = ?
            """;

    private static final String SOFT_DELETE = """
            UPDATE habitos SET activo = FALSE WHERE id_habito = ? AND id_usuario = ?
            """;

    public List<Habito> findByUsuario(long idUsuario) throws SQLException {
        List<Habito> habitos = new ArrayList<>();
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USUARIO)) {
            statement.setLong(1, idUsuario);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) habitos.add(map(result));
            }
        }
        return habitos;
    }

    public Optional<Habito> findByIdAndUsuario(long id, long idUsuario) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_AND_USUARIO)) {
            statement.setLong(1, id);
            statement.setLong(2, idUsuario);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public long insert(long idUsuario, long idCategoria, String nombre, String descripcion, String tipoMeta,
                       BigDecimal metaDiaria, String unidadMedida, LocalDate fechaCreacion) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, idUsuario);
            statement.setLong(2, idCategoria);
            statement.setString(3, nombre);
            statement.setString(4, descripcion);
            statement.setString(5, tipoMeta);
            if (metaDiaria == null) statement.setNull(6, java.sql.Types.DECIMAL);
            else statement.setBigDecimal(6, metaDiaria);
            statement.setString(7, unidadMedida);
            statement.setDate(8, Date.valueOf(fechaCreacion));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new SQLException("No se obtuvo el id del hábito");
            }
        }
    }

    public boolean update(long id, long idUsuario, long idCategoria, String nombre, String descripcion, String tipoMeta,
                          BigDecimal metaDiaria, String unidadMedida) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setLong(1, idCategoria);
            statement.setString(2, nombre);
            statement.setString(3, descripcion);
            statement.setString(4, tipoMeta);
            if (metaDiaria == null) statement.setNull(5, java.sql.Types.DECIMAL);
            else statement.setBigDecimal(5, metaDiaria);
            statement.setString(6, unidadMedida);
            statement.setLong(7, id);
            statement.setLong(8, idUsuario);
            return statement.executeUpdate() > 0;
        }
    }

    public boolean softDelete(long id, long idUsuario) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(SOFT_DELETE)) {
            statement.setLong(1, id);
            statement.setLong(2, idUsuario);
            return statement.executeUpdate() > 0;
        }
    }

    private Habito map(ResultSet result) throws SQLException {
        return new Habito(
                result.getLong("id_habito"),
                result.getLong("id_usuario"),
                result.getLong("id_categoria"),
                result.getString("nombre"),
                result.getString("descripcion"),
                result.getString("categoria"),
                result.getString("tipo_meta"),
                result.getBigDecimal("meta_diaria"),
                result.getString("unidad_medida"),
                result.getBoolean("activo"),
                result.getDate("fecha_creacion").toLocalDate());
    }
}