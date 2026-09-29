package com.retfinalo.controller;

import com.retfinalo.api.ApiResult;
import com.retfinalo.model.Habito;
import com.retfinalo.service.HabitoService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HabitoController {
    private final HabitoService service = new HabitoService();

    public ApiResult listar(long idUsuario) {
        try {
            List<Map<String, Object>> habitos = new ArrayList<>();
            for (Habito habito : service.listar(idUsuario)) habitos.add(aJson(habito));
            return ApiResult.ok(habitos);
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible cargar los hábitos");
        }
    }

    public ApiResult crear(long idUsuario, Map<String, Object> body) {
        try {
            long id = service.crear(idUsuario, entero(body, "idCategoria"), texto(body, "nombre"),
                    texto(body, "descripcion"), texto(body, "tipoMeta"), decimal(body, "metaDiaria"), texto(body, "unidadMedida"));
            Habito creado = service.buscar(id, idUsuario).orElseThrow();
            return ApiResult.created(aJson(creado));
        } catch (IllegalArgumentException e) {
            return ApiResult.error(400, e.getMessage());
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible crear el hábito");
        }
    }

    public ApiResult actualizar(long id, long idUsuario, Map<String, Object> body) {
        try {
            boolean modificado = service.actualizar(id, idUsuario, entero(body, "idCategoria"), texto(body, "nombre"),
                    texto(body, "descripcion"), texto(body, "tipoMeta"), decimal(body, "metaDiaria"), texto(body, "unidadMedida"));
            if (!modificado) return ApiResult.error(404, "El hábito no existe o no te pertenece");
            return service.buscar(id, idUsuario)
                    .map(h -> ApiResult.ok(aJson(h)))
                    .orElse(ApiResult.error(404, "El hábito no existe o no te pertenece"));
        } catch (IllegalArgumentException e) {
            return ApiResult.error(400, e.getMessage());
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible actualizar el hábito");
        }
    }

    public ApiResult eliminar(long id, long idUsuario) {
        try {
            return service.eliminar(id, idUsuario) ? ApiResult.noContent()
                    : ApiResult.error(404, "El hábito no existe o no te pertenece");
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible eliminar el hábito");
        }
    }

    private Map<String, Object> aJson(Habito habito) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", habito.id());
        json.put("idCategoria", habito.idCategoria());
        json.put("categoria", habito.categoria());
        json.put("nombre", habito.nombre());
        json.put("descripcion", habito.descripcion());
        json.put("tipoMeta", habito.tipoMeta());
        json.put("metaDiaria", habito.metaDiaria());
        json.put("unidadMedida", habito.unidadMedida());
        json.put("activo", habito.activo());
        json.put("fechaCreacion", habito.fechaCreacion().toString());
        return json;
    }

    private String texto(Map<String, Object> body, String clave) {
        Object valor = body.get(clave);
        return valor == null ? null : String.valueOf(valor);
    }

    private long entero(Map<String, Object> body, String clave) {
        Object valor = body.get(clave);
        if (valor == null) throw new IllegalArgumentException("El campo " + clave + " es obligatorio");
        try {
            return Long.parseLong(String.valueOf(valor));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + clave + " no es un número válido");
        }
    }

    private BigDecimal decimal(Map<String, Object> body, String clave) {
        Object valor = body.get(clave);
        if (valor == null || String.valueOf(valor).isBlank()) return null;
        try {
            return new BigDecimal(String.valueOf(valor));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + clave + " no es un número válido");
        }
    }
}