package com.rafael.controle_financeiro.repository;

import com.rafael.controle_financeiro.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}