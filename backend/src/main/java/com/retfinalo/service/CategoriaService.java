package com.retfinalo.service;

import com.retfinalo.model.Categoria;
import com.retfinalo.repository.CategoriaRepository;

import java.sql.SQLException;
import java.util.List;

public class CategoriaService {
    private final CategoriaRepository repository = new CategoriaRepository();

    public List<Categoria> listar() throws SQLException {
        return repository.findAll();
    }
}