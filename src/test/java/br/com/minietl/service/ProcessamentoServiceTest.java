package br.com.minietl.service;

import br.com.minietl.config.AppProperties;
import br.com.minietl.domain.entity.Processamento;
import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;
import br.com.minietl.domain.repository.ClienteRepository;
import br.com.minietl.domain.repository.ProcessamentoRepository;
import br.com.minietl.domain.repository.RegistroProcessamentoRepository;
import br.com.minietl.dto.PreviaDto;
import br.com.minietl.exception.ArquivoInvalidoException;
import br.com.minietl.extract.CsvExtractor;
import br.com.minietl.transform.DuplicateDetector;
import br.com.minietl.transform.TransformTestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static br.com.minietl.support.CsvTestUtils.recurso;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Testes do serviço de processamento com repositórios simulados (Mockito). */
@ExtendWith(MockitoExtension.class)
class ProcessamentoServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProcessamentoRepository processamentoRepository;
    @Mock
    private RegistroProcessamentoRepository registroRepository;

    private ProcessamentoService service;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties("x.db", new AppProperties.Preview(2),
                new AppProperties.Export(','));
        service = new ProcessamentoService(new CsvExtractor(), TransformTestFactory.pipeline(),
                new DuplicateDetector(), clienteRepository, processamentoRepository, registroRepository, properties);
    }

    @Test
    @SuppressWarnings("unchecked")
    void processaClassificaEPersisteTodosOsRegistros() {
        when(clienteRepository.mapearIdsPorEmail(anyCollection())).thenReturn(Map.of());
        when(processamentoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Processamento p = service.processar("misto.csv", recurso("misto.csv"));

        assertThat(p.getQtdExtraidos()).isEqualTo(7);
        assertThat(p.getQtdValidos()).isEqualTo(3);
        assertThat(p.getQtdInvalidos()).isEqualTo(3);
        assertThat(p.getQtdDuplicados()).isEqualTo(1);

        ArgumentCaptor<List<RegistroProcessamento>> captor = ArgumentCaptor.forClass(List.class);
        verify(registroRepository).saveAll(captor.capture());
        List<RegistroProcessamento> registros = captor.getValue();
        assertThat(registros).hasSize(7);

        RegistroProcessamento primeiro = registros.get(0);
        assertThat(primeiro.getOrigNome()).isEqualTo("  maria da silva  ");
        assertThat(primeiro.getTransNome()).isEqualTo("Maria da Silva");
        assertThat(primeiro.getTransEmail()).isEqualTo("maria@exemplo.com");

        RegistroProcessamento duplicado = registros.get(4);
        assertThat(duplicado.getSituacao()).isEqualTo(SituacaoRegistro.DUPLICADO);
        assertThat(duplicado.getDuplicadoDaLinha()).isEqualTo(2);
    }

    @Test
    void consideraEmailsJaCadastradosComoDuplicados() {
        when(clienteRepository.mapearIdsPorEmail(anyCollection())).thenReturn(Map.of("joao@exemplo.com", 9L));
        when(processamentoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Processamento p = service.processar("misto.csv", recurso("misto.csv"));

        assertThat(p.getQtdValidos()).isEqualTo(2);
        assertThat(p.getQtdDuplicados()).isEqualTo(2);
    }

    @Test
    void previaNaoGravaNadaELimitaAsLinhas() {
        PreviaDto previa = service.gerarPrevia("misto.csv", recurso("misto.csv"));

        assertThat(previa.totalRegistros()).isEqualTo(7);
        assertThat(previa.linhas()).hasSize(2);
        assertThat(previa.truncada()).isTrue();
        assertThat(previa.comProblemaEstrutura()).isEqualTo(1);
        verifyNoInteractions(processamentoRepository, registroRepository, clienteRepository);
    }

    @Test
    void rejeitaArquivoSemExtensaoCsv() {
        assertThatThrownBy(() -> service.processar("dados.xlsx", recurso("misto.csv")))
                .isInstanceOf(ArquivoInvalidoException.class).hasMessageContaining(".csv");
        verify(processamentoRepository, never()).save(any());
    }

    @Test
    void naoPersisteNadaQuandoArquivoEhInvalido() {
        assertThatThrownBy(() -> service.processar("x.csv", recurso("sem_coluna_cidade.csv")))
                .isInstanceOf(ArquivoInvalidoException.class);
        verifyNoInteractions(processamentoRepository, registroRepository);
    }
}
