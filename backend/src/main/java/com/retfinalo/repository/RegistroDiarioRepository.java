package com.retfinalo.repository;

import com.retfinalo.config.DatabaseConnection;
import com.retfinalo.model.RegistroDiario;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegistroDiarioRepository {
    private static final String REGISTRAR = "{ CALL sp_registrar_habito_diario(?, ?, ?, ?) }";

    private static final String FIND_BY_HABITO = """
            SELECT id_registro, id_habito, fecha, valor_completado, cumplido, nota
            FROM registros_diarios
            WHERE id_habito = ?
            ORDER BY fecha DESC
            """;

    /** Upsert del avance diario vía el procedimiento de integridad de la base de datos. */
    public void registrar(long idHabito, String fecha, BigDecimal valor, String nota) throws SQLException {
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(REGISTRAR)) {
            statement.setLong(1, idHabito);
            statement.setDate(2, java.sql.Date.valueOf(fecha));
            statement.setBigDecimal(3, valor);
            statement.setString(4, nota);
            statement.execute();
        }
    }

    public List<RegistroDiario> findByHabito(long idHabito) throws SQLException {
        List<RegistroDiario> registros = new ArrayList<>();
        try (Connection connection = DatabaseConnection.open();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_HABITO)) {
            statement.setLong(1, idHabito);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    registros.add(new RegistroDiario(
                            result.getLong("id_registro"),
                            result.getLong("id_habito"),
                            result.getDate("fecha").toString(),
                            result.getBigDecimal("valor_completado"),
                            result.getBoolean("cumplido"),
                            result.getString("nota")));
                }
            }
        }
        return registros;
    }
}