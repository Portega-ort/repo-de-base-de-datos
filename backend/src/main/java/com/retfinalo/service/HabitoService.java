package com.retfinalo.service;

import com.retfinalo.model.Habito;
import com.retfinalo.repository.HabitoRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HabitoService {
    private static final String PATTERN_VALIDO = "^(BOOLEANO|CUANTITATIVO)$";
    private final HabitoRepository repository = new HabitoRepository();

    public List<Habito> listar(long idUsuario) throws SQLException {
        return repository.findByUsuario(idUsuario);
    }

    public Optional<Habito> buscar(long id, long idUsuario) throws SQLException {
        return repository.findByIdAndUsuario(id, idUsuario);
    }

    public long crear(long idUsuario, long idCategoria, String nombre, String descripcion, String tipoMeta,
                      BigDecimal metaDiaria, String unidadMedida) throws SQLException {
        validarCampos(nombre, tipoMeta, metaDiaria, unidadMedida);
        return repository.insert(idUsuario, idCategoria, nombre, normalizar(descripcion), tipoMeta, metaDiaria,
                normalizar(unidadMedida), LocalDate.now());
    }

    public boolean actualizar(long id, long idUsuario, long idCategoria, String nombre, String descripcion, String tipoMeta,
                              BigDecimal metaDiaria, String unidadMedida) throws SQLException {
        validarCampos(nombre, tipoMeta, metaDiaria, unidadMedida);
        return repository.update(id, idUsuario, idCategoria, nombre, normalizar(descripcion), tipoMeta, metaDiaria,
                normalizar(unidadMedida));
    }

    public boolean eliminar(long id, long idUsuario) throws SQLException {
        return repository.softDelete(id, idUsuario);
    }

    private void validarCampos(String nombre, String tipoMeta, BigDecimal metaDiaria, String unidadMedida) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre del hábito es obligatorio");
        if (tipoMeta == null || !tipoMeta.matches(PATTERN_VALIDO)) {
            throw new IllegalArgumentException("tipoMeta debe ser BOOLEANO o CUANTITATIVO");
        }
        if ("CUANTITATIVO".equals(tipoMeta)) {
            if (metaDiaria == null || metaDiaria.signum() <= 0) {
                throw new IllegalArgumentException("En un hábito cuantitativo la meta diaria debe ser mayor que 0");
            }
            if (unidadMedida == null || unidadMedida.isBlank()) {
                throw new IllegalArgumentException("En un hábito cuantitativo la unidad de medida es obligatoria");
            }
        }
    }

    private String normalizar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}