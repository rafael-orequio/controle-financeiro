package com.rafael.controle_financeiro.controller;

import com.rafael.controle_financeiro.dto.TransacaoRequest;
import com.rafael.controle_financeiro.dto.TransacaoResponse;
import com.rafael.controle_financeiro.service.TransacaoService;
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
@RequestMapping("/transacoes")
public class TransacaoController {

    private final TransacaoService service;

    public TransacaoController(TransacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<TransacaoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(usuarioId(jwt));
    }

    @GetMapping("/{id}")
    public TransacaoResponse buscar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.buscar(usuarioId(jwt), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoResponse criar(@AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody TransacaoRequest request) {
        return service.criar(usuarioId(jwt), request);
    }

    @PutMapping("/{id}")
    public TransacaoResponse atualizar(@AuthenticationPrincipal Jwt jwt,
                                       @PathVariable Long id,
                                       @Valid @RequestBody TransacaoRequest request) {
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