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
    public List<TransacaoResponse> listar() {
        return repository.findAll().stream()
                .map(TransacaoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscar(Long id) {
        return TransacaoResponse.de(buscarEntidade(id));
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request) {
        Categoria categoria = validarCategoria(request);
        Transacao transacao = new Transacao(
                request.descricao(), request.valor(), request.data(), request.tipo(), categoria);
        return TransacaoResponse.de(repository.save(transacao));
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest request) {
        Transacao transacao = buscarEntidade(id);
        Categoria categoria = validarCategoria(request);
        transacao.setDescricao(request.descricao());
        transacao.setValor(request.valor());
        transacao.setData(request.data());
        transacao.setTipo(request.tipo());
        transacao.setCategoria(categoria);
        return TransacaoResponse.de(transacao);
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(buscarEntidade(id));
    }

    private Transacao buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transação não encontrada: " + id));
    }

    private Categoria validarCategoria(TransacaoRequest request) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada: " + request.categoriaId()));
        if (categoria.getTipo() != request.tipo()) {
            throw new RegraDeNegocioException(
                    "O tipo da transação deve ser igual ao tipo da categoria (" + categoria.getTipo() + ")");
        }
        return categoria;
    }
}