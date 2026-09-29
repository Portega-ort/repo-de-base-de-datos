package com.retfinalo.model;

public record Usuario(long id, String nombre, String apellido, String email, String passwordHash, String fechaRegistro, boolean esAdmin) {
    public static Usuario sinHash(Usuario u) {
        return new Usuario(u.id(), u.nombre(), u.apellido(), u.email(), null, u.fechaRegistro(), u.esAdmin());
    }
}