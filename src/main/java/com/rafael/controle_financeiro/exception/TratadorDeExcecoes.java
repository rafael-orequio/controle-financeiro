package com.rafael.controle_financeiro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class TratadorDeExcecoes {

    public record ErroResposta(int status, String mensagem, List<String> detalhes) {
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex) {
        ErroResposta erro = new ErroResposta(404, ex.getMessage(), List.of());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        ErroResposta erro = new ErroResposta(400, "Dados inválidos", detalhes);
        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> corpoInvalido(HttpMessageNotReadableException ex) {
        ErroResposta erro = new ErroResposta(400, "Corpo da requisição inválido", List.of());
        return ResponseEntity.badRequest().body(erro);
    }
}