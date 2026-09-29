package com.retfinalo.controller;

import com.retfinalo.api.ApiResult;
import com.retfinalo.model.Categoria;
import com.retfinalo.service.CategoriaService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CategoriaController {
    private final CategoriaService service = new CategoriaService();

    public ApiResult listar() {
        try {
            List<Map<String, Object>> categorias = new ArrayList<>();
            for (Categoria categoria : service.listar()) {
                Map<String, Object> json = new LinkedHashMap<>();
                json.put("id", categoria.id());
                json.put("nombre", categoria.nombre());
                json.put("descripcion", categoria.descripcion());
                json.put("colorHex", categoria.colorHex());
                categorias.add(json);
            }
            return ApiResult.ok(categorias);
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible cargar las categorías");
        }
    }
}