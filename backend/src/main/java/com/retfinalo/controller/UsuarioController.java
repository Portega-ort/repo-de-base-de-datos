package com.retfinalo.controller;

import com.retfinalo.api.ApiResult;
import com.retfinalo.auth.SessionStore;
import com.retfinalo.model.Usuario;
import com.retfinalo.service.UsuarioService;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class UsuarioController {
    private final UsuarioService service = new UsuarioService();
    private final SessionStore sessions = SessionStore.INSTANCE;

    public ApiResult registrar(Map<String, Object> body) {
        try {
            Usuario usuario = service.registrar(texto(body, "nombre"), texto(body, "apellido"), texto(body, "email"), texto(body, "password"));
            return ApiResult.created(sesion(usuario));
        } catch (IllegalArgumentException e) {
            return ApiResult.error(400, e.getMessage());
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible registrar la cuenta");
        }
    }

    public ApiResult iniciarSesion(Map<String, Object> body) {
        try {
            return service.autenticar(texto(body, "email"), texto(body, "password"))
                    .map(this::sesion)
                    .map(ApiResult::ok)
                    .orElse(ApiResult.error(401, "Correo o contraseña incorrectos"));
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible iniciar sesión");
        }
    }

    public ApiResult cerrarSesion(String token) {
        sessions.revoke(token);
        return ApiResult.noContent();
    }

    public ApiResult perfil(long idUsuario) {
        try {
            Usuario usuario = service.buscar(idUsuario);
            return usuario == null ? ApiResult.error(404, "Usuario no encontrado") : ApiResult.ok(perfil(usuario));
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible cargar el perfil");
        }
    }

    public ApiResult actualizarPerfil(long idUsuario, Map<String, Object> body) {
        try {
            Usuario usuario = service.actualizarPerfil(idUsuario, texto(body, "nombre"), texto(body, "apellido"),
                    texto(body, "email"), texto(body, "password"));
            return ApiResult.ok(perfil(usuario));
        } catch (IllegalArgumentException e) {
            return ApiResult.error(400, e.getMessage());
        } catch (SQLException e) {
            return ApiResult.error(500, "No fue posible actualizar el perfil");
        }
    }

    private Map<String, Object> sesion(Usuario usuario) {
        LinkedHashMap<String, Object> json = new LinkedHashMap<>(perfil(usuario));
        json.put("token", sessions.create(usuario.id()));
        return json;
    }

    private Map<String, Object> perfil(Usuario usuario) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", usuario.id());
        json.put("nombre", usuario.nombre());
        json.put("apellido", usuario.apellido());
        json.put("email", usuario.email());
        json.put("fechaRegistro", usuario.fechaRegistro());
        json.put("esAdmin", usuario.esAdmin());
        return json;
    }

    private String texto(Map<String, Object> body, String clave) {
        Object valor = body.get(clave);
        return valor == null ? null : String.valueOf(valor);
    }
}