package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.CategoriaRequest;
import com.rafael.controle_financeiro.dto.CategoriaResponse;
import com.rafael.controle_financeiro.exception.CredenciaisInvalidasException;
import com.rafael.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.rafael.controle_financeiro.exception.RegraDeNegocioException;
import com.rafael.controle_financeiro.model.Categoria;
import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.model.Usuario;
import com.rafael.controle_financeiro.repository.CategoriaRepository;
import com.rafael.controle_financeiro.repository.TransacaoRepository;
import com.rafael.controle_financeiro.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    private static final Long USUARIO_ID = 10L;

    @Mock
    private CategoriaRepository repository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CategoriaService service;

    private final Usuario usuario = new Usuario("Rafael", "rafael@exemplo.com", "hash");

    @Test
    @DisplayName("Cria a categoria para o usuário autenticado")
    void criaCategoriaParaOUsuario() {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(repository.save(any(Categoria.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        CategoriaResponse resposta = service.criar(
                USUARIO_ID, new CategoriaRequest("Mercado", TipoTransacao.DESPESA));

        assertThat(resposta.nome()).isEqualTo("Mercado");
        assertThat(resposta.tipo()).isEqualTo(TipoTransacao.DESPESA);
        verify(repository).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Recusa criar categoria quando o usuário do token não existe mais")
    void recusaCriarCategoriaQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(
                USUARIO_ID, new CategoriaRequest("Mercado", TipoTransacao.DESPESA)))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verify(repository, never()).save(any(Categoria.class));
    }

    @Test
    @DisplayName("Não encontra a categoria de outro usuário")
    void naoEncontraCategoriaDeOutroUsuario() {
        when(repository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(USUARIO_ID, 1L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Categoria não encontrada");
    }

    @Test
    @DisplayName("Bloqueia a remoção quando existem transações vinculadas")
    void bloqueiaRemocaoQuandoExistemTransacoes() {
        Categoria categoria = new Categoria("Mercado", TipoTransacao.DESPESA, usuario);
        when(repository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.existsByCategoriaId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.remover(USUARIO_ID, 1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("existem transações vinculadas");

        verify(repository, never()).delete(any(Categoria.class));
    }

    @Test
    @DisplayName("Remove a categoria quando não há transações vinculadas")
    void removeCategoriaSemTransacoes() {
        Categoria categoria = new Categoria("Mercado", TipoTransacao.DESPESA, usuario);
        when(repository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.existsByCategoriaId(1L)).thenReturn(false);

        service.remover(USUARIO_ID, 1L);

        verify(repository).delete(categoria);
    }
}