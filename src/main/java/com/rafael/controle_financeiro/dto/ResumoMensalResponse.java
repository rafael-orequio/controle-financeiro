package com.rafael.controle_financeiro.dto;

import java.util.List;

public record ResumoMensalResponse(int ano, int mes, List<ResumoCategoriaResponse> categorias) {
}