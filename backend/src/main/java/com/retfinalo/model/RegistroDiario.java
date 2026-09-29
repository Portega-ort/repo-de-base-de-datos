package com.retfinalo.model;

import java.math.BigDecimal;

public record RegistroDiario(long id, long idHabito, String fecha, BigDecimal valor, boolean cumplido, String nota) {
}