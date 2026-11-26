package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.ResumoCategoriaResponse;
import com.rafael.controle_financeiro.dto.ResumoMensalResponse;
import com.rafael.controle_financeiro.exception.RegraDeNegocioException;
import com.rafael.controle_financeiro.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
public class ResumoMensalService {

    private final TransacaoRepository repository;

    public ResumoMensalService(TransacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ResumoMensalResponse resumir(Long usuarioId, Integer ano, Integer mes) {
        YearMonth atual = YearMonth.now();
        int anoConsultado = ano != null ? ano : atual.getYear();
        int mesConsultado = mes != null ? mes : atual.getMonthValue();

        if (mesConsultado < 1 || mesConsultado > 12) {
            throw new RegraDeNegocioException("Mês inválido: informe um valor de 1 a 12");
        }
        if (anoConsultado < 2000 || anoConsultado > 2100) {
            throw new RegraDeNegocioException("Ano inválido: informe um valor entre 2000 e 2100");
        }

        YearMonth periodo = YearMonth.of(anoConsultado, mesConsultado);

        List<ResumoCategoriaResponse> categorias = repository
                .resumoPorCategoria(usuarioId, periodo.atDay(1), periodo.atEndOfMonth())
                .stream()
                .map(resumo -> new ResumoCategoriaResponse(
                        resumo.getCategoriaId(),
                        resumo.getCategoriaNome(),
                        resumo.getTipo(),
                        resumo.getTotal(),
                        resumo.getQuantidade()))
                .toList();

        return new ResumoMensalResponse(anoConsultado, mesConsultado, categorias);
    }
}