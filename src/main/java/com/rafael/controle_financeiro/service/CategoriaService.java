package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.CategoriaRequest;
import com.rafael.controle_financeiro.dto.CategoriaResponse;
import com.rafael.controle_financeiro.exception.CredenciaisInvalidasException;
import com.rafael.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.rafael.controle_financeiro.exception.RegraDeNegocioException;
import com.rafael.controle_financeiro.model.Categoria;
import com.rafael.controle_financeiro.model.Usuario;
import com.rafael.controle_financeiro.repository.CategoriaRepository;
import com.rafael.controle_financeiro.repository.TransacaoRepository;
import com.rafael.controle_financeiro.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;

    public CategoriaService(CategoriaRepository repository,
                            TransacaoRepository transacaoRepository,
                            UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(Long usuarioId) {
        return repository.findByUsuarioId(usuarioId).stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long usuarioId, Long id) {
        return CategoriaResponse.de(buscarEntidade(usuarioId, id));
    }

    @Transactional
    public CategoriaResponse criar(Long usuarioId, CategoriaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new CredenciaisInvalidasException(
                        "Sessão inválida: faça login novamente"));
        Categoria categoria = new Categoria(request.nome(), request.tipo(), usuario);
        return CategoriaResponse.de(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long usuarioId, Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(usuarioId, id);
        categoria.setNome(request.nome());
        categoria.setTipo(request.tipo());
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void remover(Long usuarioId, Long id) {
        Categoria categoria = buscarEntidade(usuarioId, id);
        if (transacaoRepository.existsByCategoriaId(id)) {
            throw new RegraDeNegocioException(
                    "Não é possível remover a categoria: existem transações vinculadas a ela");
        }
        repository.delete(categoria);
    }

    private Categoria buscarEntidade(Long usuarioId, Long id) {
        return repository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
    }
}