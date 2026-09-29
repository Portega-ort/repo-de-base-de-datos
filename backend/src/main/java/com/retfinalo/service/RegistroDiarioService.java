package com.retfinalo.service;

import com.retfinalo.model.RegistroDiario;
import com.retfinalo.repository.RegistroDiarioRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RegistroDiarioService {
    private final RegistroDiarioRepository repository = new RegistroDiarioRepository();

    public void registrar(long idHabito, String fecha, BigDecimal valor, String nota) throws SQLException {
        if (fecha == null || fecha.isBlank()) fecha = LocalDate.now().toString();
        try {
            LocalDate.parse(fecha);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("La fecha debe tener formato AAAA-MM-DD");
        }
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("El valor del avance debe ser mayor o igual a 0");
        }
        repository.registrar(idHabito, fecha, valor, normalizar(nota));
    }

    public List<RegistroDiario> listarDeHabito(long idHabito) throws SQLException {
        return repository.findByHabito(idHabito);
    }

    private String normalizar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}