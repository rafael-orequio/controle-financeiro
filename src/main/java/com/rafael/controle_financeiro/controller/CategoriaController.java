package com.rafael.controle_financeiro.controller;

import com.rafael.controle_financeiro.dto.CategoriaRequest;
import com.rafael.controle_financeiro.dto.CategoriaResponse;
import com.rafael.controle_financeiro.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@Valid @RequestBody CategoriaRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}