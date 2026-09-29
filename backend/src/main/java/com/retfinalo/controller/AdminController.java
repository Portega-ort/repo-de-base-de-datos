package com.retfinalo.controller;

import com.retfinalo.api.ApiResult;
import com.retfinalo.service.AdminService;
import com.retfinalo.service.UsuarioService;

import java.sql.SQLException;

public class AdminController {
    private final AdminService adminService = new AdminService();
    private final UsuarioService usuarioService = new UsuarioService();

    public ApiResult reporte(long idUsuario) {
        try {
            if (!usuarioService.esAdmin(idUsuario)) {
                return ApiResult.error(403, "No tienes permisos de administrador");
            }
            return ApiResult.ok(adminService.reporteGlobal());
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible generar el reporte");
        }
    }
}