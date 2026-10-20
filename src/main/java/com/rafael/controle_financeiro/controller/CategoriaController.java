package com.rafael.controle_financeiro.controller;

import com.rafael.controle_financeiro.dto.CategoriaRequest;
import com.rafael.controle_financeiro.dto.CategoriaResponse;
import com.rafael.controle_financeiro.service.CategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public List<CategoriaResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(usuarioId(jwt));
    }

    @GetMapping("/{id}")
    public CategoriaResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.buscar(usuarioId(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaResponse criar(@AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody CategoriaRequest request) {
        return service.criar(usuarioId(jwt), request);
    }

    @PutMapping("/{id}")
    public CategoriaResponse atualizar(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable Long id,
                                       @Valid @RequestBody CategoriaRequest request) {
        return service.atualizar(usuarioId(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.remover(usuarioId(jwt), id);
    }

    private Long usuarioId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}