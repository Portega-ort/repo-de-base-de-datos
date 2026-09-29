package com.retfinalo.repository;

import com.retfinalo.config.DatabaseConnection;
import com.retfinalo.model.Categoria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaRepository {
    private static final String FIND_ALL = """
            SELECT id_categoria, nombre, descripcion, color_hex FROM categorias ORDER BY nombre
            """;

    public List<Categoria> findAll() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                categorias.add(new Categoria(result.getLong("id_categoria"), result.getString("nombre"),
                        result.getString("descripcion"), result.getString("color_hex")));
            }
        }
        return categorias;
    }
}