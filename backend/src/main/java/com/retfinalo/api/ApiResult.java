package com.retfinalo.api;

/** Respuesta de un controlador: estado HTTP + cuerpo serializable a JSON. */
public record ApiResult(int status, Object body) {
    public static ApiResult ok(Object body) {
        return new ApiResult(200, body);
    }

    public static ApiResult created(Object body) {
        return new ApiResult(201, body);
    }

    public static ApiResult noContent() {
        return new ApiResult(204, null);
    }

    public static ApiResult error(int status, String message) {
        return new ApiResult(status, java.util.Map.of("error", message));
    }
}