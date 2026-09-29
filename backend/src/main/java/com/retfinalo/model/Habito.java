package com.retfinalo.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Habito(long id, long idUsuario, long idCategoria, String nombre, String descripcion, String categoria, String tipoMeta, BigDecimal metaDiaria, String unidadMedida, boolean activo, LocalDate fechaCreacion) {
}