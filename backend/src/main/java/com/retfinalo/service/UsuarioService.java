package com.retfinalo.service;

import com.retfinalo.auth.PasswordUtil;
import com.retfinalo.model.Usuario;
import com.retfinalo.repository.UsuarioRepository;

import java.sql.SQLException;
import java.util.Optional;

public class UsuarioService {
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private final UsuarioRepository repository = new UsuarioRepository();

    public Usuario registrar(String nombre, String apellido, String email, String password) throws SQLException {
        validarDatos(nombre, apellido, email, password);
        email = email.trim().toLowerCase();
        if (repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }
        long id = repository.insert(nombre.trim(), apellido.trim(), email, PasswordUtil.hash(password));
        return repository.findById(id).orElseThrow();
    }

    public Optional<Usuario> autenticar(String email, String password) throws SQLException {
        if (email == null || password == null) return Optional.empty();
        Optional<Usuario> usuario = repository.findByEmail(email.trim().toLowerCase());
        return usuario.filter(u -> PasswordUtil.verify(password, u.passwordHash()));
    }

    public Usuario buscar(long id) throws SQLException {
        return repository.findById(id).map(Usuario::sinHash).orElse(null);
    }

    public boolean esAdmin(long id) throws SQLException {
        return repository.findById(id).map(Usuario::esAdmin).orElse(false);
    }

    public Usuario actualizarPerfil(long id, String nombre, String apellido, String email, String nuevaPassword) throws SQLException {
        validarNombreApellidoEmail(nombre, apellido, email);
        email = email.trim().toLowerCase();
        Optional<Usuario> ocupado = repository.findByEmail(email);
        if (ocupado.isPresent() && ocupado.get().id() != id) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }
        repository.updateProfile(id, nombre.trim(), apellido.trim(), email);
        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            actualizarPassword(id, nuevaPassword);
        }
        return repository.findById(id).map(Usuario::sinHash).orElseThrow();
    }

    private void actualizarPassword(long id, String password) throws SQLException {
        if (password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una letra y un número");
        }
        repository.updatePassword(id, PasswordUtil.hash(password));
    }

    private void validarDatos(String nombre, String apellido, String email, String password) {
        validarNombreApellidoEmail(nombre, apellido, email);
        if (password == null || password.length() < 8 || !password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres, una letra y un número");
        }
    }

    private void validarNombreApellidoEmail(String nombre, String apellido, String email) {
        if (nombre == null || nombre.isBlank() || apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El nombre y el apellido son obligatorios");
        }
        if (email == null || !email.trim().matches(EMAIL_PATTERN)) {
            throw new IllegalArgumentException("El correo electrónico no es válido");
        }
    }
}