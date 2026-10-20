package com.rafael.controle_financeiro.controller;

import com.rafael.controle_financeiro.dto.SaldoResponse;
import com.rafael.controle_financeiro.service.SaldoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saldo")
public class SaldoController {

    private final SaldoService service;

    public SaldoController(SaldoService service) {
        this.service = service;
    }

    @GetMapping
    public SaldoResponse saldo(@AuthenticationPrincipal Jwt jwt) {
        return service.calcular(Long.valueOf(jwt.getSubject()));
    }
}