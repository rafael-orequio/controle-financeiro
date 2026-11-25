package com.rafael.controle_financeiro.service;

import com.rafael.controle_financeiro.dto.SaldoResponse;
import com.rafael.controle_financeiro.model.TipoTransacao;
import com.rafael.controle_financeiro.repository.TransacaoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaldoServiceTest {

    private static final Long USUARIO_ID = 10L;

    @Mock
    private TransacaoRepository repository;

    @InjectMocks
    private SaldoService service;

    @Test
    @DisplayName("Calcula o saldo como receitas menos despesas")
    void calculaSaldoComReceitasEDespesas() {
        when(repository.totaisPorTipo(USUARIO_ID)).thenReturn(List.of(
                total(TipoTransacao.RECEITA, "2500.00"),
                total(TipoTransacao.DESPESA, "470.75")));

        SaldoResponse saldo = service.calcular(USUARIO_ID);

        assertThat(saldo.totalReceitas()).isEqualByComparingTo("2500.00");
        assertThat(saldo.totalDespesas()).isEqualByComparingTo("470.75");
        assertThat(saldo.saldo()).isEqualByComparingTo("2029.25");
    }

    @Test
    @DisplayName("Devolve tudo zerado quando o usuário não tem transações")
    void devolveZeroQuandoNaoHaTransacoes() {
        when(repository.totaisPorTipo(USUARIO_ID)).thenReturn(List.of());

        SaldoResponse saldo = service.calcular(USUARIO_ID);

        assertThat(saldo.totalReceitas()).isEqualByComparingTo("0.00");
        assertThat(saldo.totalDespesas()).isEqualByComparingTo("0.00");
        assertThat(saldo.saldo()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Devolve despesas zeradas quando só há receitas")
    void devolveDespesasZeradasQuandoSoHaReceitas() {
        when(repository.totaisPorTipo(USUARIO_ID)).thenReturn(List.of(
                total(TipoTransacao.RECEITA, "1000.00")));

        SaldoResponse saldo = service.calcular(USUARIO_ID);

        assertThat(saldo.totalDespesas()).isEqualByComparingTo("0.00");
        assertThat(saldo.saldo()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("Devolve saldo negativo quando as despesas superam as receitas")
    void devolveSaldoNegativoQuandoDespesasSuperamReceitas() {
        when(repository.totaisPorTipo(USUARIO_ID)).thenReturn(List.of(
                total(TipoTransacao.RECEITA, "100.00"),
                total(TipoTransacao.DESPESA, "350.50")));

        SaldoResponse saldo = service.calcular(USUARIO_ID);

        assertThat(saldo.saldo()).isEqualByComparingTo("-250.50");
    }

    private TransacaoRepository.TotalPorTipo total(TipoTransacao tipo, String valor) {
        return new TransacaoRepository.TotalPorTipo() {
            @Override
            public TipoTransacao getTipo() {
                return tipo;
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal(valor);
            }
        };
    }
}