package br.com.minietl.web;

import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.service.ProcessamentoService;
import br.com.minietl.support.IntegracaoTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static br.com.minietl.support.CsvTestUtils.lerExportado;
import static br.com.minietl.support.CsvTestUtils.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Testes dos endpoints web (MockMvc + SQLite). */
@AutoConfigureMockMvc
class EndpointsTest extends IntegracaoTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ProcessamentoService processamentoService;

    private MockMultipartFile arquivo(String nome) {
        return new MockMultipartFile("arquivo", nome, "text/csv", recurso(nome));
    }

    @Test
    void dashboardSemProcessamentos() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(content().string(containsString("Nenhum processamento realizado")));
    }

    @Test
    void dashboardComIndicadoresEGraficos() throws Exception {
        processamentoService.processar("misto.csv", recurso("misto.csv"));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("graficoDistribuicao")))
                .andExpect(content().string(containsString("misto.csv")));
    }

    @Test
    void paginaDeImportacao() throws Exception {
        mvc.perform(get("/importar")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Importar arquivo CSV")));
    }

    @Test
    void previaMostraRegistrosSemGravar() throws Exception {
        mvc.perform(multipart("/importar/previa").file(arquivo("misto.csv")).header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Prévia")))
                .andExpect(content().string(containsString("maria da silva")))
                .andExpect(content().string(containsString("problema de estrutura")));

        assertThat(processamentoRepository.count()).isZero();
    }

    @Test
    void previaInformaArquivoInvalido() throws Exception {
        mvc.perform(multipart("/importar/previa").file(arquivo("sem_coluna_cidade.csv")).header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Arquivo inválido")))
                .andExpect(content().string(containsString("Colunas obrigatórias ausentes: Cidade")));
    }

    @Test
    void processarRedirecionaParaODetalhe() throws Exception {
        mvc.perform(multipart("/importar/processar").file(arquivo("misto.csv")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/processamentos/*"))
                .andExpect(flash().attributeExists("sucesso"));

        assertThat(processamentoRepository.count()).isEqualTo(1);
    }

    @Test
    void processarArquivoInvalidoVoltaComErro() throws Exception {
        MockMultipartFile txt = new MockMultipartFile("arquivo", "dados.txt", "text/plain", recurso("misto.csv"));

        mvc.perform(multipart("/importar/processar").file(txt))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("erro", containsString(".csv")));
    }

    @Test
    void detalheDoProcessamentoComAreaDeExportacao() throws Exception {
        Processamento p = processamentoService.processar("misto.csv", recurso("misto.csv"));

        mvc.perform(get("/processamentos/{id}", p.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CSV tratado")))
                .andExpect(content().string(containsString("CSV de rejeitados")))
                .andExpect(content().string(containsString("CSV completo")))
                .andExpect(content().string(containsString("Carregar válidos no banco")));

        mvc.perform(get("/processamentos/{id}", p.getId()).param("situacao", "DUPLICADO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("já informado na linha 2")))
                .andExpect(content().string(not(containsString("E-mail inválido"))));
    }

    @Test
    void cargaPeloEndpoint() throws Exception {
        Processamento p = processamentoService.processar("misto.csv", recurso("misto.csv"));

        mvc.perform(post("/processamentos/{id}/carga", p.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("sucesso", containsString("3 registro(s) carregado(s)")));
        mvc.perform(post("/processamentos/{id}/carga", p.getId()))
                .andExpect(flash().attribute("erro", containsString("já foi carregado")));

        assertThat(clienteRepository.count()).isEqualTo(3);
    }

    @Test
    void downloadDasTresExportacoes() throws Exception {
        Processamento p = processamentoService.processar("misto.csv", recurso("misto.csv"));

        for (String tipo : new String[]{"tratado", "rejeitados", "completo"}) {
            MvcResult resultado = mvc.perform(get("/processamentos/{id}/exportacao/{tipo}", p.getId(), tipo))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("text/csv")))
                    .andExpect(header().string("Content-Disposition",
                            containsString("attachment; filename=\"" + tipo + "_proc" + p.getId() + ".csv\"")))
                    .andReturn();
            assertThat(lerExportado(resultado.getResponse().getContentAsByteArray())).isNotEmpty();
        }
    }

    @Test
    void exportacaoDesconhecidaOuInexistenteRetorna404() throws Exception {
        Processamento p = processamentoService.processar("misto.csv", recurso("misto.csv"));

        mvc.perform(get("/processamentos/{id}/exportacao/xpto", p.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/processamentos/{id}/exportacao/tratado", 9999)).andExpect(status().isNotFound());
        mvc.perform(get("/processamentos/9999")).andExpect(status().isNotFound())
                .andExpect(content().string(containsString("não encontrado")));
    }

    @Test
    void historicoListaProcessamentos() throws Exception {
        processamentoService.processar("misto.csv", recurso("misto.csv"));
        processamentoService.processar("validos.csv", recurso("validos.csv"));

        mvc.perform(get("/processamentos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("misto.csv")))
                .andExpect(content().string(containsString("validos.csv")));
    }

    @Test
    void consultaDeClientesComPesquisa() throws Exception {
        Processamento p = processamentoService.processar("validos.csv", recurso("validos.csv"));
        mvc.perform(post("/processamentos/{id}/carga", p.getId()));

        mvc.perform(get("/clientes").param("q", "joão"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("joao@exemplo.com")))
                .andExpect(content().string(not(containsString("maria@exemplo.com"))));

        mvc.perform(get("/clientes").param("q", "ANA@").header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("clientes :: resultado"))
                .andExpect(content().string(containsString("ana@exemplo.com")));
    }

    @Test
    void detalheDoCliente() throws Exception {
        Processamento p = processamentoService.processar("validos.csv", recurso("validos.csv"));
        mvc.perform(post("/processamentos/{id}/carga", p.getId()));
        Long id = clienteRepository.findAll().get(0).getId();

        mvc.perform(get("/clientes/{id}", id)).andExpect(status().isOk())
                .andExpect(content().string(containsString("Data de criação")));
        mvc.perform(get("/clientes/{id}", 99999)).andExpect(status().isNotFound());
    }

    @Test
    void arquivoDeExemploDisponivel() throws Exception {
        MvcResult r = mvc.perform(get("/exemplo/clientes_exemplo.csv")).andExpect(status().isOk()).andReturn();
        assertThat(new String(r.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8))
                .startsWith("Nome,E-mail,Telefone,Cidade");
    }
}
