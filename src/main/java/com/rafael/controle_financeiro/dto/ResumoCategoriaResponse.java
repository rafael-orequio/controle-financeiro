package com.rafael.controle_financeiro.dto;

import com.rafael.controle_financeiro.model.TipoTransacao;

import java.math.BigDecimal;

public record ResumoCategoriaResponse(
        Long categoriaId,
        String categoriaNome,
        TipoTransacao tipo,
        BigDecimal total,
        long quantidade
) {
}