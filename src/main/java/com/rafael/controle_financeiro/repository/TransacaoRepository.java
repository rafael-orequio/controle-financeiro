package com.rafael.controle_financeiro.repository;

import com.rafael.controle_financeiro.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
}