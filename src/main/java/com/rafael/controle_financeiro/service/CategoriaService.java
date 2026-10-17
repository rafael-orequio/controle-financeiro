package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.CategoriaRequest;
import com.rafael.controle_financeiro.dto.CategoriaResponse;
import com.rafael.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.rafael.controle_financeiro.model.Categoria;
import com.rafael.controle_financeiro.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository repository;

    public CategoriaService(CategoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return repository.findAll().stream()
                .map(CategoriaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscar(Long id) {
        return CategoriaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        Categoria categoria = new Categoria(request.nome(), request.tipo());
        return CategoriaResponse.de(repository.save(categoria));
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(id);
        categoria.setNome(request.nome());
        categoria.setTipo(request.tipo());
        return CategoriaResponse.de(categoria);
    }

    @Transactional
    public void remover(Long id) {
        repository.delete(buscarEntidade(id));
    }

    private Categoria buscarEntidade(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada: " + id));
    }
}