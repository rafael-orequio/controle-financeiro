package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.TransacaoRequest;
import com.rafael.controle_financeiro.dto.TransacaoResponse;
import com.rafael.controle_financeiro.exception.RecursoNaoEncontradoException;
import com.rafael.controle_financeiro.exception.RegraDeNegocioException;
import com.rafael.controle_financeiro.model.Categoria;
import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.model.Transacao;
import com.rafael.controle_financeiro.model.Usuario;
import com.rafael.controle_financeiro.repository.CategoriaRepository;
import com.rafael.controle_financeiro.repository.TransacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    private static final Long USUARIO_ID = 10L;

    @Mock
    private TransacaoRepository repository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private TransacaoService service;

    private final Usuario usuario = new Usuario("Rafael", "rafael@exemplo.com", "hash");

    @Test
    @DisplayName("Cria a transação quando o tipo combina com o da categoria")
    void criaTransacaoQuandoTipoCombinaComCategoria() {
        Categoria categoria = new Categoria("Mercado", TipoTransacao.DESPESA, usuario);
        when(categoriaRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(categoria));
        when(repository.save(any(Transacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        TransacaoRequest request = new TransacaoRequest(
                "Padaria", new BigDecimal("120.25"), LocalDate.of(2026, 10, 8), TipoTransacao.DESPESA, 1L);

        TransacaoResponse resposta = service.criar(USUARIO_ID, request);

        assertThat(resposta.descricao()).isEqualTo("Padaria");
        assertThat(resposta.valor()).isEqualByComparingTo("120.25");
        assertThat(resposta.tipo()).isEqualTo(TipoTransacao.DESPESA);
        assertThat(resposta.categoriaNome()).isEqualTo("Mercado");
        verify(repository).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Recusa a transação quando o tipo é diferente do tipo da categoria")
    void recusaTransacaoQuandoTipoDiferenteDaCategoria() {
        Categoria categoria = new Categoria("Salário", TipoTransacao.RECEITA, usuario);
        when(categoriaRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(categoria));
        TransacaoRequest request = new TransacaoRequest(
                "Padaria", new BigDecimal("120.25"), LocalDate.of(2026, 10, 8), TipoTransacao.DESPESA, 1L);

        assertThatThrownBy(() -> service.criar(USUARIO_ID, request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("igual ao tipo da categoria");

        verify(repository, never()).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Recusa a transação quando a categoria não é do usuário")
    void recusaTransacaoQuandoCategoriaNaoEDoUsuario() {
        when(categoriaRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.empty());
        TransacaoRequest request = new TransacaoRequest(
                "Padaria", new BigDecimal("120.25"), LocalDate.of(2026, 10, 8), TipoTransacao.DESPESA, 1L);

        assertThatThrownBy(() -> service.criar(USUARIO_ID, request))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("Categoria não encontrada");

        verify(repository, never()).save(any(Transacao.class));
    }
}