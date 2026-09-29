package com.retfinalo.controller;

import com.retfinalo.api.ApiResult;
import com.retfinalo.model.Habito;
import com.retfinalo.model.RegistroDiario;
import com.retfinalo.service.HabitoService;
import com.retfinalo.service.RegistroDiarioService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RegistroDiarioController {
    private final RegistroDiarioService service = new RegistroDiarioService();
    private final HabitoService habitoService = new HabitoService();

    public ApiResult registrar(long idUsuario, Map<String, Object> body) {
        try {
            long idHabito = Long.parseLong(String.valueOf(body.get("idHabito")));
            Habito habito = habitoService.buscar(idHabito, idUsuario).orElse(null);
            if (habito == null) return ApiResult.error(404, "El hábito no existe o no te pertenece");
            BigDecimal valor = body.get("valor") == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(body.get("valor")));
            String nota = body.get("nota") == null ? null : String.valueOf(body.get("nota"));
            String fecha = body.get("fecha") == null ? null : String.valueOf(body.get("fecha"));
            service.registrar(idHabito, fecha, valor, nota);
            return ApiResult.ok(Map.of("mensaje", "Avance registrado"));
        } catch (NumberFormatException e) {
            return ApiResult.error(400, "idHabito o valor no válidos");
        } catch (IllegalArgumentException e) {
            return ApiResult.error(400, e.getMessage());
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible registrar el avance");
        }
    }

    public ApiResult listar(long idUsuario, long idHabito) {
        try {
            Habito habito = habitoService.buscar(idHabito, idUsuario).orElse(null);
            if (habito == null) return ApiResult.error(404, "El hábito no existe o no te pertenece");
            List<Map<String, Object>> registros = new ArrayList<>();
            for (RegistroDiario registro : service.listarDeHabito(idHabito)) {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("id", registro.id());
                json.put("idHabito", registro.idHabito());
                json.put("fecha", registro.fecha());
                json.put("valor", registro.valor());
                json.put("cumplido", registro.cumplido());
                json.put("nota", registro.nota());
                json.put("metaDiaria", habito.metaDiaria());
                json.put("unidadMedida", habito.unidadMedida());
                registros.add(json);
            }
            return ApiResult.ok(registros);
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible cargar el progreso");
        }
    }
}