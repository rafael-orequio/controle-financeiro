package com.rafael.controle_financeiro.dto;

import java.math.BigDecimal;

public record SaldoResponse(
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {
}