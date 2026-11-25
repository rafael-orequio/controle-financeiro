package com.rafael.controle_financeiro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Rotas protegidas exigem token")
    void rotasProtegidasExigemToken() throws Exception {
        mockMvc.perform(get("/categorias")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/transacoes")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/saldo")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Cadastro e login devolvem um token que abre as rotas protegidas")
    void cadastroELoginDevolvemTokenValido() throws Exception {
        String email = novoEmail("rafael");

        MvcResult cadastro = mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro("Rafael", email, "senhaforte123")))
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(corpo(cadastro)).contains(email).doesNotContain("senha");

        String token = login(email, "senhaforte123");
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/categorias").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Login com senha errada devolve 401")
    void loginComSenhaErradaDevolve401() throws Exception {
        String email = novoEmail("rafael");
        registrar("Rafael", email, "senhaforte123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin(email, "senhaerrada1")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Não permite cadastrar o mesmo e-mail duas vezes")
    void naoPermiteEmailDuplicado() throws Exception {
        String email = novoEmail("rafael");
        registrar("Rafael", email, "senhaforte123");

        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro("Outro Rafael", email, "senhaforte123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Um usuário não enxerga nem altera os dados de outro")
    void usuarioNaoVeDadosDeOutroUsuario() throws Exception {
        String tokenRafael = criarUsuarioELogar("Rafael");
        String tokenMaria = criarUsuarioELogar("Maria");
        String categoriaDoRafael = criarCategoria(tokenRafael, "Salario", "RECEITA");

        MvcResult listaDaMaria = mockMvc.perform(get("/categorias")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenMaria)))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(corpo(listaDaMaria)).isEqualTo("[]");

        mockMvc.perform(get("/categorias/" + categoriaDoRafael)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenMaria)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/categorias/" + categoriaDoRafael)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenMaria)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/transacoes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenMaria))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTransacao("Invasao", "10.00", "RECEITA", categoriaDoRafael)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("O saldo soma apenas as transações do próprio usuário")
    void saldoSomaApenasOsDadosDoProprioUsuario() throws Exception {
        String tokenRafael = criarUsuarioELogar("Rafael");
        String tokenMaria = criarUsuarioELogar("Maria");

        String receitaDoRafael = criarCategoria(tokenRafael, "Salario", "RECEITA");
        String despesaDoRafael = criarCategoria(tokenRafael, "Mercado", "DESPESA");
        criarTransacao(tokenRafael, "Salario de outubro", "2500.00", "RECEITA", receitaDoRafael);
        criarTransacao(tokenRafael, "Compras da semana", "350.50", "DESPESA", despesaDoRafael);

        String receitaDaMaria = criarCategoria(tokenMaria, "Freelance", "RECEITA");
        criarTransacao(tokenMaria, "Projeto de site", "1000.00", "RECEITA", receitaDaMaria);

        MvcResult saldoDoRafael = consultarSaldo(tokenRafael);
        assertThat(numero(saldoDoRafael, "totalReceitas")).isEqualByComparingTo("2500.00");
        assertThat(numero(saldoDoRafael, "totalDespesas")).isEqualByComparingTo("350.50");
        assertThat(numero(saldoDoRafael, "saldo")).isEqualByComparingTo("2149.50");

        MvcResult saldoDaMaria = consultarSaldo(tokenMaria);
        assertThat(numero(saldoDaMaria, "totalReceitas")).isEqualByComparingTo("1000.00");
        assertThat(numero(saldoDaMaria, "totalDespesas")).isEqualByComparingTo("0.00");
        assertThat(numero(saldoDaMaria, "saldo")).isEqualByComparingTo("1000.00");
    }

    // ---------- Funções de apoio ----------

    private String novoEmail(String prefixo) {
        return prefixo + "-" + UUID.randomUUID() + "@exemplo.com";
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private void registrar(String nome, String email, String senha) throws Exception {
        mockMvc.perform(post("/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro(nome, email, senha)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin(email, senha)))
                .andExpect(status().isOk())
                .andReturn();
        return extrair(resultado, "\"token\"\\s*:\\s*\"([^\"]+)\"");
    }

    private String criarUsuarioELogar(String nome) throws Exception {
        String email = novoEmail(nome.toLowerCase());
        registrar(nome, email, "senhaforte123");
        return login(email, "senhaforte123");
    }

    private String criarCategoria(String token, String nome, String tipo) throws Exception {
        String json = """
                {"nome": "%s", "tipo": "%s"}
                """.formatted(nome, tipo);
        MvcResult resultado = mockMvc.perform(post("/categorias")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return extrair(resultado, "\"id\"\\s*:\\s*(\\d+)");
    }

    private void criarTransacao(String token, String descricao, String valor,
                                String tipo, String categoriaId) throws Exception {
        mockMvc.perform(post("/transacoes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonTransacao(descricao, valor, tipo, categoriaId)))
                .andExpect(status().isCreated());
    }

    private MvcResult consultarSaldo(String token) throws Exception {
        return mockMvc.perform(get("/saldo").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private String jsonRegistro(String nome, String email, String senha) {
        return """
                {"nome": "%s", "email": "%s", "senha": "%s"}
                """.formatted(nome, email, senha);
    }

    private String jsonLogin(String email, String senha) {
        return """
                {"email": "%s", "senha": "%s"}
                """.formatted(email, senha);
    }

    private String jsonTransacao(String descricao, String valor, String tipo, String categoriaId) {
        return """
                {"descricao": "%s", "valor": %s, "data": "2026-10-05", "tipo": "%s", "categoriaId": %s}
                """.formatted(descricao, valor, tipo, categoriaId);
    }

    private String corpo(MvcResult resultado) throws Exception {
        return resultado.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String extrair(MvcResult resultado, String regex) throws Exception {
        String conteudo = corpo(resultado);
        Matcher encontrado = Pattern.compile(regex).matcher(conteudo);
        assertThat(encontrado.find())
                .as("padrão %s não encontrado em %s", regex, conteudo)
                .isTrue();
        return encontrado.group(1);
    }

    private BigDecimal numero(MvcResult resultado, String campo) throws Exception {
        return new BigDecimal(extrair(resultado, "\"" + campo + "\"\\s*:\\s*(-?[0-9.]+)"));
    }
}