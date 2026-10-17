package com.rafael.controle_financeiro.dto;

import com.rafael.controle_financeiro.model.Categoria;
import com.rafael.controle_financeiro.model.TipoTransacao;

public record CategoriaResponse(Long id, String nome, TipoTransacao tipo) {

    public static CategoriaResponse de(Categoria categoria) {
        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getTipo());
    }
}