package com.retfinalo;

import com.retfinalo.api.ApiResult;
import com.retfinalo.auth.SessionStore;
import com.retfinalo.config.DatabaseConnection;
import com.retfinalo.controller.AdminController;
import com.retfinalo.controller.CategoriaController;
import com.retfinalo.controller.HabitoController;
import com.retfinalo.controller.RegistroDiarioController;
import com.retfinalo.controller.UsuarioController;
import com.retfinalo.util.Json;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;

/** Servidor de desarrollo para Habit Tracker (API REST + estáticos). */
public class Main {
    private static final Path FRONTEND_DIRECTORY = Path.of("frontend", "src").toAbsolutePath().normalize();
    private static final Map<String, String> CONTENT_TYPES = Map.of("html", "text/html; charset=UTF-8", "css", "text/css; charset=UTF-8", "js", "application/javascript; charset=UTF-8", "svg", "image/svg+xml");
    private static final SessionStore SESSIONS = SessionStore.INSTANCE;
    private static final UsuarioController USUARIOS = new UsuarioController();
    private static final AdminController ADMIN = new AdminController();
    private static final CategoriaController CATEGORIAS = new CategoriaController();
    private static final HabitoController HABITOS = new HabitoController();
    private static final RegistroDiarioController REGISTROS = new RegistroDiarioController();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", Main::api);
        server.createContext("/", Main::serveFrontend);
        server.start();
        System.out.println("Habit Tracker listo en http://localhost:8080");
    }

    private static void api(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        try {
            if ("GET".equals(method) && path.equals("/api/health")) {
                health(exchange);
            } else if ("POST".equals(method) && path.equals("/api/auth/register")) {
                responder(exchange, USUARIOS.registrar(cuerpo(exchange)));
            } else if ("POST".equals(method) && path.equals("/api/auth/login")) {
                responder(exchange, USUARIOS.iniciarSesion(cuerpo(exchange)));
            } else if ("POST".equals(method) && path.equals("/api/auth/logout")) {
                responder(exchange, USUARIOS.cerrarSesion(token(exchange)));
            } else if (path.equals("/api/auth/me")) {
                long id = autenticar(exchange);
                if ("GET".equals(method)) {
                    responder(exchange, USUARIOS.perfil(id));
                } else if ("PUT".equals(method)) {
                    responder(exchange, USUARIOS.actualizarPerfil(id, cuerpo(exchange)));
                } else {
                    metodoNoPermitido(exchange, "GET, PUT");
                }
            } else if ("GET".equals(method) && path.equals("/api/categorias")) {
                responder(exchange, CATEGORIAS.listar());
            } else if ("GET".equals(method) && path.equals("/api/admin")) {
                responder(exchange, ADMIN.reporte(autenticar(exchange)));
            } else if (path.equals("/api/habitos")) {
                long id = autenticar(exchange);
                if ("GET".equals(method)) {
                    responder(exchange, HABITOS.listar(id));
                } else if ("POST".equals(method)) {
                    responder(exchange, HABITOS.crear(id, cuerpo(exchange)));
                } else {
                    metodoNoPermitido(exchange, "GET, POST");
                }
            } else if (path.startsWith("/api/habitos/") && path.endsWith("/registros")) {
                long id = autenticar(exchange);
                String idHabito = path.substring("/api/habitos/".length(), path.length() - "/registros".length());
                if ("GET".equals(method)) {
                    responder(exchange, REGISTROS.listar(id, Long.parseLong(idHabito)));
                } else {
                    metodoNoPermitido(exchange, "GET");
                }
            } else if (path.startsWith("/api/habitos/")) {
                long id = autenticar(exchange);
                String idHabito = path.substring("/api/habitos/".length());
                if ("PUT".equals(method)) {
                    responder(exchange, HABITOS.actualizar(numero(idHabito), id, cuerpo(exchange)));
                } else if ("DELETE".equals(method)) {
                    responder(exchange, HABITOS.eliminar(numero(idHabito), id));
                } else {
                    metodoNoPermitido(exchange, "PUT, DELETE");
                }
            } else if ("POST".equals(method) && path.equals("/api/registros")) {
                responder(exchange, REGISTROS.registrar(autenticar(exchange), cuerpo(exchange)));
            } else {
                responder(exchange, ApiResult.error(404, "Ruta no encontrada"));
            }
        } catch (NumberFormatException e) {
            responder(exchange, ApiResult.error(400, "Identificador inválido"));
        } catch (NoAutorizado e) {
            responder(exchange, ApiResult.error(401, "Se requiere iniciar sesión"));
        } catch (Exception e) {
            e.printStackTrace();
            responder(exchange, ApiResult.error(500, "Error interno del servidor"));
        }
    }

    private static void health(HttpExchange exchange) throws IOException {
        try (Connection ignored = DatabaseConnection.open()) {
            sendJson(exchange, 200, "{\"status\":\"ok\",\"message\":\"MySQL conectado\"}");
        } catch (Exception exception) {
            sendJson(exchange, 503, "{\"status\":\"error\",\"message\":\"No fue posible conectar con MySQL\"}");
        }
    }

    private static long autenticar(HttpExchange exchange) {
        long userId = SESSIONS.userId(token(exchange));
        if (userId == -1) throw new NoAutorizado();
        return userId;
    }

    private static String token(HttpExchange exchange) {
        String cabecera = exchange.getRequestHeaders().getFirst("Authorization");
        return cabecera != null && cabecera.startsWith("Bearer ") ? cabecera.substring(7) : null;
    }

    private static long numero(String texto) {
        return Long.parseLong(texto.trim());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> cuerpo(HttpExchange exchange) throws IOException {
        String texto = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (texto.isBlank()) return Map.of();
        Object valor = Json.parse(texto);
        if (valor instanceof Map) return (Map<String, Object>) valor;
        throw new IllegalArgumentException("El cuerpo debe ser un objeto JSON");
    }

    private static void responder(HttpExchange exchange, ApiResult resultado) throws IOException {
        if (resultado.body() == null) {
            exchange.sendResponseHeaders(resultado.status(), -1);
            exchange.close();
        } else {
            sendJson(exchange, resultado.status(), Json.stringify(resultado.body()));
        }
    }

    private static void metodoNoPermitido(HttpExchange exchange, String permitidos) throws IOException {
        exchange.getResponseHeaders().set("Allow", permitidos);
        sendJson(exchange, 405, "{\"error\":\"Método no permitido\"}");
    }

    private static void serveFrontend(HttpExchange exchange) throws IOException {
        String requested = exchange.getRequestURI().getPath();
        Path file = FRONTEND_DIRECTORY.resolve("/".equals(requested) ? "index.html" : requested.substring(1)).normalize();
        if (!file.startsWith(FRONTEND_DIRECTORY) || !Files.isRegularFile(file)) { exchange.sendResponseHeaders(404, -1); exchange.close(); return; }
        String name = file.getFileName().toString();
        String extension = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1) : "";
        exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPES.getOrDefault(extension, "application/octet-stream"));
        exchange.sendResponseHeaders(200, Files.size(file));
        Files.copy(file, exchange.getResponseBody());
        exchange.close();
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static final class NoAutorizado extends RuntimeException {
        private NoAutorizado() {
            super("Se requiere iniciar sesión");
        }
    }
}