package com.rafael.controle_financeiro.dto;

import com.rafael.controle_financeiro.model.Usuario;

public record UsuarioResponse(Long id, String nome, String email) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail());
    }
}