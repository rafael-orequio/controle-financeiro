package com.rafael.controle_financeiro.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos) {
}