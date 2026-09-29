package com.retfinalo.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Centraliza la configuración de conexión a MySQL mediante variables de entorno. */
public final class DatabaseConnection {
    private DatabaseConnection() {
    }

    public static Connection open() throws SQLException {
        String url = getRequired("DB_URL");
        String user = getRequired("DB_USER");
        String password = getRequired("DB_PASSWORD");
        return DriverManager.getConnection(url, user, password);
    }

    private static String getRequired(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno " + key + ". Revisa backend/.env.example");
        }
        return value;
    }
}
