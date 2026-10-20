package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.TransacaoRequest;
import com.rafael.controle_financeiro.dto.TransacaoResponse;
import com.rafael.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.rafael.controle_financeiro.exception.RegraDeNegocioException;
import com.rafael.controle_financeiro.model.Categoria;
import com.rafael.controle_financeiro.model.Transacao;
import com.rafael.controle_financeiro.repository.CategoriaRepository;
import com.rafael.controle_financeiro.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransacaoService {

    private final TransacaoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(TransacaoRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar(Long usuarioId) {
        return repository.findByCategoriaUsuarioId(usuarioId).stream()
                .map(TransacaoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscar(Long usuarioId, Long id) {
        return TransacaoResponse.de(buscarEntidade(usuarioId, id));
    }

    @Transactional
    public TransacaoResponse criar(Long usuarioId, TransacaoRequest request) {
        Categoria categoria = validarCategoria(usuarioId, request);
        Transacao transacao = new Transacao(
                request.descricao(), request.valor(), request.data(), request.tipo(), categoria);
        return TransacaoResponse.de(repository.save(transacao));
    }

    @Transactional
    public TransacaoResponse atualizar(Long usuarioId, Long id, TransacaoRequest request) {
        Transacao transacao = buscarEntidade(usuarioId, id);
        Categoria categoria = validarCategoria(usuarioId, request);
        transacao.setDescricao(request.descricao());
        transacao.setValor(request.valor());
        transacao.setData(request.data());
        transacao.setTipo(request.tipo());
        transacao.setCategoria(categoria);
        return TransacaoResponse.de(transacao);
    }

    @Transactional
    public void remover(Long usuarioId, Long id) {
        repository.delete(buscarEntidade(usuarioId, id));
    }

    private Transacao buscarEntidade(Long usuarioId, Long id) {
        return repository.findByIdAndCategoriaUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada: " + id));
    }

    private Categoria validarCategoria(Long usuarioId, TransacaoRequest request) {
        Categoria categoria = categoriaRepository.findByIdAndUsuarioId(request.categoriaId(), usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada: " + request.categoriaId()));
        if (categoria.getTipo() != request.tipo()) {
            throw new RegraDeNegocioException(
                    "O tipo da transação deve ser igual ao tipo da categoria (" + categoria.getTipo() + ")");
        }
        return categoria;
    }
}