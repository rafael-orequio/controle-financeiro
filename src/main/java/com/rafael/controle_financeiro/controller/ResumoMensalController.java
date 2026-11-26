package com.rafael.controle_financeiro.controller;

import com.rafael.controle_financeiro.dto.ResumoMensalResponse;
import com.rafael.controle_financeiro.service.ResumoMensalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/resumo-mensal")
public class ResumoMensalController {

    private final ResumoMensalService service;

    public ResumoMensalController(ResumoMensalService service) {
        this.service = service;
    }

    @GetMapping
    public ResumoMensalResponse resumo(@AuthenticationPrincipal Jwt jwt,
                                       @RequestParam(required = false) Integer ano,
                                       @RequestParam(required = false) Integer mes) {
        return service.resumir(Long.valueOf(jwt.getSubject()), ano, mes);
    }
}