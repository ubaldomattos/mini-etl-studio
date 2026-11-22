package br.com.minietl.domain.entity;

import br.com.minietl.domain.enums.SituacaoRegistro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Um registro (linha) de um processamento. Guarda, lado a lado, os dados originais
 * (imutáveis após a extração) e os dados transformados, além do resultado das validações.
 * É a fonte das três exportações, inclusive a partir do histórico.
 */
@Entity
@Table(name = "registro_processamento")
public class RegistroProcessamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "processamento_id", nullable = false)
    private Processamento processamento;

    @Column(name = "numero_linha", nullable = false)
    private int numeroLinha;

    // ---- dados originais ----
    @Column(name = "orig_nome")
    private String origNome;
    @Column(name = "orig_email")
    private String origEmail;
    @Column(name = "orig_telefone")
    private String origTelefone;
    @Column(name = "orig_cidade")
    private String origCidade;
    @Column(name = "linha_bruta")
    private String linhaBruta;

    // ---- dados transformados ----
    @Column(name = "trans_nome")
    private String transNome;
    @Column(name = "trans_email")
    private String transEmail;
    @Column(name = "trans_telefone")
    private String transTelefone;
    @Column(name = "trans_cidade")
    private String transCidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoRegistro situacao;

    @Column(name = "resultado_validacao")
    private String resultadoValidacao;

    @Column(name = "mensagem")
    private String mensagem;

    @Column(name = "duplicado_da_linha")
    private Integer duplicadoDaLinha;

    @Column(name = "cliente_id")
    private Long clienteId;

    protected RegistroProcessamento() {
    }

    public RegistroProcessamento(Processamento processamento, int numeroLinha) {
        this.processamento = processamento;
        this.numeroLinha = numeroLinha;
    }

    public void definirOriginais(String nome, String email, String telefone, String cidade, String linhaBruta) {
        this.origNome = nome;
        this.origEmail = email;
        this.origTelefone = telefone;
        this.origCidade = cidade;
        this.linhaBruta = linhaBruta;
    }

    public void definirTransformados(String nome, String email, String telefone, String cidade) {
        this.transNome = nome;
        this.transEmail = email;
        this.transTelefone = telefone;
        this.transCidade = cidade;
    }

    public void definirResultado(SituacaoRegistro situacao, String resultadoValidacao, String mensagem,
                                 Integer duplicadoDaLinha) {
        this.situacao = situacao;
        this.resultadoValidacao = resultadoValidacao;
        this.mensagem = mensagem;
        this.duplicadoDaLinha = duplicadoDaLinha;
    }

    public void vincularCliente(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getId() {
        return id;
    }

    public int getNumeroLinha() {
        return numeroLinha;
    }

    public String getOrigNome() {
        return origNome;
    }

    public String getOrigEmail() {
        return origEmail;
    }

    public String getOrigTelefone() {
        return origTelefone;
    }

    public String getOrigCidade() {
        return origCidade;
    }

    public String getLinhaBruta() {
        return linhaBruta;
    }

    public String getTransNome() {
        return transNome;
    }

    public String getTransEmail() {
        return transEmail;
    }

    public String getTransTelefone() {
        return transTelefone;
    }

    public String getTransCidade() {
        return transCidade;
    }

    public SituacaoRegistro getSituacao() {
        return situacao;
    }

    public String getResultadoValidacao() {
        return resultadoValidacao;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Integer getDuplicadoDaLinha() {
        return duplicadoDaLinha;
    }

    public Long getClienteId() {
        return clienteId;
    }
}
