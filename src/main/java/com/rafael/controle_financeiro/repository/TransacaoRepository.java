package com.rafael.controle_financeiro.repository;

import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.model.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository
        extends JpaRepository<Transacao, Long>, JpaSpecificationExecutor<Transacao> {

    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Transacao> findAll(Specification<Transacao> spec, Pageable pageable);

    Optional<Transacao> findByIdAndCategoriaUsuarioId(Long id, Long usuarioId);

    boolean existsByCategoriaId(Long categoriaId);

    @Query("""
            select t.tipo as tipo, sum(t.valor) as total
            from Transacao t
            where t.categoria.usuario.id = :usuarioId
            group by t.tipo
            """)
    List<TotalPorTipo> totaisPorTipo(@Param("usuarioId") Long usuarioId);

    @Query("""
            select c.id as categoriaId, c.nome as categoriaNome, t.tipo as tipo,
                   sum(t.valor) as total, count(t) as quantidade
            from Transacao t
            join t.categoria c
            where c.usuario.id = :usuarioId
              and t.data >= :inicio
              and t.data <= :fim
            group by c.id, c.nome, t.tipo
            order by sum(t.valor) desc
            """)
    List<ResumoPorCategoria> resumoPorCategoria(@Param("usuarioId") Long usuarioId,
                                                @Param("inicio") LocalDate inicio,
                                                @Param("fim") LocalDate fim);

    interface TotalPorTipo {
        TipoTransacao getTipo();

        BigDecimal getTotal();
    }

    interface ResumoPorCategoria {
        Long getCategoriaId();

        String getCategoriaNome();

        TipoTransacao getTipo();

        BigDecimal getTotal();

        Long getQuantidade();
    }
}