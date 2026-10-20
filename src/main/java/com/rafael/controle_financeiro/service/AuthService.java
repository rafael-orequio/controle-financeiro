package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.LoginRequest;
import com.rafael.controle_financeiro.dto.TokenResponse;
import com.rafael.controle_financeiro.exception.CredenciaisInvalidasException;
import com.rafael.controle_financeiro.model.Usuario;
import com.rafael.controle_financeiro.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UsuarioRepository repository,
                       PasswordEncoder passwordEncoder,
                       TokenService tokenService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Usuario usuario = repository.findByEmail(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!passwordEncoder.matches(request.senha(), usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }

        return tokenService.gerar(usuario);
    }
}