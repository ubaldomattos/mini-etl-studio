package br.com.minietl.dto;

import br.com.minietl.domain.entity.Cliente;

import java.time.LocalDateTime;

public record ClienteDto(Long id, String nome, String email, String telefone, String cidade,
                         LocalDateTime dataCriacao) {

    public static ClienteDto de(Cliente c) {
        return new ClienteDto(c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), c.getCidade(),
                c.getDataCriacao());
    }
}