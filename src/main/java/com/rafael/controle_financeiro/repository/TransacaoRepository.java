package com.rafael.controle_financeiro.repository;

import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query("select t.tipo as tipo, sum(t.valor) as total from Transacao t group by t.tipo")
    List<TotalPorTipo> totaisPorTipo();

    boolean existsByCategoriaId(Long categoriaId);

    interface TotalPorTipo {
        TipoTransacao getTipo();

        BigDecimal getTotal();
    }
}