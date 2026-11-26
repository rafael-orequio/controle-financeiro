package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.PaginaResponse;
import com.rafael.controle_financeiro.dto.TransacaoRequest;
import com.rafael.controle_financeiro.dto.TransacaoResponse;
import com.rafael.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.rafael.controle_financeiro.exception.RegraDeNegocioException;
import com.rafael.controle_financeiro.model.Categoria;
import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.model.Transacao;
import com.rafael.controle_financeiro.repository.CategoriaRepository;
import com.rafael.controle_financeiro.repository.TransacaoRepository;
import com.rafael.controle_financeiro.repository.TransacaoSpecs;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class TransacaoService {

    private static final int TAMANHO_MAXIMO = 100;

    private final TransacaoRepository repository;
    private final CategoriaRepository categoriaRepository;

    public TransacaoService(TransacaoRepository repository, CategoriaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<TransacaoResponse> listar(Long usuarioId, LocalDate inicio, LocalDate fim,
                                                    TipoTransacao tipo, Long categoriaId,
                                                    int pagina, int tamanho) {
        if (inicio != null && fim != null && inicio.isAfter(fim)) {
            throw new RegraDeNegocioException("A data inicial não pode ser posterior à data final");
        }

        Pageable pageable = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO),
                Sort.by(Sort.Direction.DESC, "data", "id"));

        Specification<Transacao> filtro = TransacaoSpecs.filtrar(usuarioId, inicio, fim, tipo, categoriaId);

        return PaginaResponse.de(repository.findAll(filtro, pageable), TransacaoResponse::de);
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