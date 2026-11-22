package br.com.minietl.domain.entity;

import br.com.minietl.domain.enums.StatusProcessamento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Histórico de uma execução do fluxo ETL (uma importação). */
@Entity
@Table(name = "processamento")
public class Processamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "data_execucao", nullable = false)
    private LocalDateTime dataExecucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProcessamento status;

    @Column(name = "qtd_extraidos", nullable = false)
    private int qtdExtraidos;

    @Column(name = "qtd_validos", nullable = false)
    private int qtdValidos;

    @Column(name = "qtd_invalidos", nullable = false)
    private int qtdInvalidos;

    @Column(name = "qtd_duplicados", nullable = false)
    private int qtdDuplicados;

    @Column(name = "qtd_carregados", nullable = false)
    private int qtdCarregados;

    protected Processamento() {
    }

    public Processamento(String nomeArquivo, int qtdExtraidos, int qtdValidos, int qtdInvalidos, int qtdDuplicados) {
        this.nomeArquivo = nomeArquivo;
        this.dataExecucao = LocalDateTime.now();
        this.status = StatusProcessamento.PROCESSADO;
        this.qtdExtraidos = qtdExtraidos;
        this.qtdValidos = qtdValidos;
        this.qtdInvalidos = qtdInvalidos;
        this.qtdDuplicados = qtdDuplicados;
    }

    /** Move um registro válido para duplicado (e-mail passou a existir na base antes da carga). */
    public void reclassificarValidoComoDuplicado() {
        qtdValidos--;
        qtdDuplicados++;
    }

    public void registrarCarga(int carregados) {
        this.qtdCarregados = carregados;
        this.status = StatusProcessamento.CARREGADO;
    }

    public Long getId() {
        return id;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public LocalDateTime getDataExecucao() {
        return dataExecucao;
    }

    public StatusProcessamento getStatus() {
        return status;
    }

    public int getQtdExtraidos() {
        return qtdExtraidos;
    }

    public int getQtdValidos() {
        return qtdValidos;
    }

    public int getQtdInvalidos() {
        return qtdInvalidos;
    }

    public int getQtdDuplicados() {
        return qtdDuplicados;
    }

    public int getQtdCarregados() {
        return qtdCarregados;
    }
}
