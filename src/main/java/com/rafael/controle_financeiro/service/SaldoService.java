package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.SaldoResponse;
import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SaldoService {

    private final TransacaoRepository repository;

    public SaldoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SaldoResponse calcular(Long usuarioId) {
        BigDecimal receitas = BigDecimal.ZERO.setScale(2);
        BigDecimal despesas = BigDecimal.ZERO.setScale(2);

        for (TransacaoRepository.TotalPorTipo total : repository.totaisPorTipo(usuarioId)) {
            if (total.getTipo() == TipoTransacao.RECEITA) {
                receitas = total.getTotal();
            } else {
                despesas = total.getTotal();
            }
        }

        return new SaldoResponse(receitas, despesas, receitas.subtract(despesas));
    }
}