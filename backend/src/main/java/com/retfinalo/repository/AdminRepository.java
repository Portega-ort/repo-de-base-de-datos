package com.retfinalo.repository;

import com.retfinalo.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Reportes para la vista de administración. */
public class AdminRepository {

    private static final String RESUMEN = """
            SELECT
                (SELECT COUNT(*) FROM usuarios) AS total_usuarios,
                (SELECT COUNT(*) FROM usuarios WHERE fecha_registro >= CURDATE() - INTERVAL 7 DAY) AS altas_7dias,
                (SELECT COUNT(*) FROM habitos WHERE activo = TRUE) AS habitos_activos,
                (SELECT COUNT(*) FROM registros_diarios) AS registros_totales,
                (SELECT COALESCE(ROUND(100 * AVG(cumplido)), 0) FROM registros_diarios) AS cumplimiento_global
            """;

    private static final String ACTIVIDAD =
            "SELECT fecha, COUNT(*) AS cantidad FROM registros_diarios WHERE fecha >= CURDATE() - INTERVAL 13 DAY GROUP BY fecha ORDER BY fecha";

    private static final String CATEGORIAS =
            "SELECT c.nombre AS nombre, COUNT(h.id_habito) AS cantidad " +
                    "FROM categorias c LEFT JOIN habitos h ON h.id_categoria = c.id_categoria AND h.activo = TRUE " +
                    "GROUP BY c.id_categoria, c.nombre ORDER BY cantidad DESC, c.nombre";

    private static final String USUARIOS = """
            SELECT u.id_usuario, u.nombre, u.apellido, u.email, u.fecha_registro, u.es_admin,
                   COUNT(DISTINCT h.id_habito) AS habitos,
                   COUNT(r.id_registro) AS registros,
                   COALESCE(ROUND(100 * AVG(r.cumplido)), 0) AS cumplimiento,
                   MAX(r.fecha) AS ultimo_registro,
                   COALESCE((SELECT MAX(fn_racha_habito(h2.id_habito, CURDATE()))
                             FROM habitos h2 WHERE h2.id_usuario = u.id_usuario AND h2.activo = TRUE), 0) AS racha
            FROM usuarios u
            LEFT JOIN habitos h ON h.id_usuario = u.id_usuario
            LEFT JOIN registros_diarios r ON r.id_habito = h.id_habito
            GROUP BY u.id_usuario, u.nombre, u.apellido, u.email, u.fecha_registro, u.es_admin
            ORDER BY u.nombre, u.apellido
            """;

    public Map<String, Object> resumen() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(RESUMEN);
             ResultSet result = statement.executeQuery()) {
            result.next();
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("totalUsuarios", result.getLong("total_usuarios"));
            json.put("altas7dias", result.getLong("altas_7dias"));
            json.put("habitosActivos", result.getLong("habitos_activos"));
            json.put("registrosTotales", result.getLong("registros_totales"));
            json.put("cumplimientoGlobal", result.getLong("cumplimiento_global"));
            return json;
        }
    }

    public java.util.List<Map<String, Object>> actividad() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(ACTIVIDAD);
             ResultSet result = statement.executeQuery()) {
            java.util.List<Map<String, Object>> diaList = new java.util.ArrayList<>();
            while (result.next()) {
                Map<String, Object> dia = new LinkedHashMap<>();
                dia.put("fecha", result.getDate("fecha").toLocalDate().toString());
                dia.put("cantidad", result.getInt("cantidad"));
                diaList.add(dia);
            }
            return diaList;
        }
    }

    public java.util.List<Map<String, Object>> categorias() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(CATEGORIAS);
             ResultSet result = statement.executeQuery()) {
            java.util.List<Map<String, Object>> categoriaList = new java.util.ArrayList<>();
            while (result.next()) {
                Map<String, Object> categoria = new LinkedHashMap<>();
                categoria.put("nombre", result.getString("nombre"));
                categoria.put("cantidad", result.getInt("cantidad"));
                categoriaList.add(categoria);
            }
            return categoriaList;
        }
    }

    public java.util.List<Map<String, Object>> usuarios() throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(USUARIOS);
             ResultSet result = statement.executeQuery()) {
            java.util.List<Map<String, Object>> usuarioList = new java.util.ArrayList<>();
            while (result.next()) {
                Map<String, Object> usuario = new LinkedHashMap<>();
                usuario.put("id", result.getLong("id_usuario"));
                usuario.put("nombre", result.getString("nombre"));
                usuario.put("apellido", result.getString("apellido"));
                usuario.put("email", result.getString("email"));
                usuario.put("fechaRegistro", result.getTimestamp("fecha_registro").toLocalDateTime().toString().replace('T', ' '));
                usuario.put("esAdmin", result.getBoolean("es_admin"));
                usuario.put("habitos", result.getInt("habitos"));
                usuario.put("registros", result.getInt("registros"));
                usuario.put("cumplimiento", result.getInt("cumplimiento"));
                usuario.put("ultimoRegistro", result.getDate("ultimo_registro") == null ? null : result.getDate("ultimo_registro").toLocalDate().toString());
                usuario.put("racha", result.getInt("racha"));
                usuarioList.add(usuario);
            }
            return usuarioList;
        }
    }
}