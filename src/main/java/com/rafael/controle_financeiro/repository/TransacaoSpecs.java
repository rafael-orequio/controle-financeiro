package com.rafael.controle_financeiro.repository;

import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.model.Transacao;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TransacaoSpecs {

    private TransacaoSpecs() {
    }

    public static Specification<Transacao> filtrar(Long usuarioId, LocalDate inicio, LocalDate fim,
                                                   TipoTransacao tipo, Long categoriaId) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            condicoes.add(cb.equal(root.get("categoria").get("usuario").get("id"), usuarioId));

            if (inicio != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("data"), inicio));
            }
            if (fim != null) {
                condicoes.add(cb.lessThanOrEqualTo(root.<LocalDate>get("data"), fim));
            }
            if (tipo != null) {
                condicoes.add(cb.equal(root.get("tipo"), tipo));
            }
            if (categoriaId != null) {
                condicoes.add(cb.equal(root.get("categoria").get("id"), categoriaId));
            }

            return cb.and(condicoes.toArray(new Predicate[0]));
        };
    }
}