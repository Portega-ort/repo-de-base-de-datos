package com.retfinalo.service;

import com.retfinalo.repository.AdminRepository;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminService {
    private final AdminRepository repository = new AdminRepository();

    public Map<String, Object> reporteGlobal() throws SQLException {
        Map<String, Object> reporte = new LinkedHashMap<>();
        reporte.put("resumen", repository.resumen());
        reporte.put("actividad", repository.actividad());
        reporte.put("categorias", repository.categorias());
        reporte.put("usuarios", repository.usuarios());
        return reporte;
    }
}