package com.rafael.controle_financeiro.dto;

import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.model.Transacao;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransacaoResponse(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDate data,
        TipoTransacao tipo,
        Long categoriaId,
        String categoriaNome
) {

    public static TransacaoResponse de(Transacao t) {
        return new TransacaoResponse(
                t.getId(),
                t.getDescricao(),
                t.getValor(),
                t.getData(),
                t.getTipo(),
                t.getCategoria().getId(),
                t.getCategoria().getNome());
    }
}