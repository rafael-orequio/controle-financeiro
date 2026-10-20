package com.rafael.controle_financeiro.repository;

import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByCategoriaUsuarioId(Long usuarioId);

    Optional<Transacao> findByIdAndCategoriaUsuarioId(Long id, Long usuarioId);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("""
            select t.tipo as tipo, sum(t.valor) as total
            from Transacao t
            where t.categoria.usuario.id = :usuarioId
            group by t.tipo
            """)
    List<TotalPorTipo> totaisPorTipo(@Param("usuarioId") Long usuarioId);

    interface TotalPorTipo {
        TipoTransacao getTipo();

        BigDecimal getTotal();
    }
}