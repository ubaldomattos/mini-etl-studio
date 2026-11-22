package br.com.minietl.dto;

import br.com.minietl.domain.entity.RegistroProcessamento;
import br.com.minietl.domain.enums.SituacaoRegistro;

/** Visão de um registro de processamento com dados originais e transformados. */
public record RegistroDto(
        int numeroLinha,
        String origNome, String origEmail, String origTelefone, String origCidade,
        String transNome, String transEmail, String transTelefone, String transCidade,
        SituacaoRegistro situacao,
        String resultadoValidacao,
        String mensagem,
        Integer duplicadoDaLinha,
        Long clienteId) {

    public static RegistroDto de(RegistroProcessamento r) {
        return new RegistroDto(r.getNumeroLinha(),
                r.getOrigNome(), r.getOrigEmail(), r.getOrigTelefone(), r.getOrigCidade(),
                r.getTransNome(), r.getTransEmail(), r.getTransTelefone(), r.getTransCidade(),
                r.getSituacao(), r.getResultadoValidacao(), r.getMensagem(), r.getDuplicadoDaLinha(),
                r.getClienteId());
    }
}