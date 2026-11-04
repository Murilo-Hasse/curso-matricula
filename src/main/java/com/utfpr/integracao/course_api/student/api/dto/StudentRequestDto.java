package com.utfpr.integracao.course_api.student.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentRequestDto (

    @NotBlank(message = "Nome Obrigatório.")
    @Size(min = 2, message = "O nome deve ter no mínimo 2 caracteres.")
    String nome,

    @NotBlank(message = "E-mail Obrigatório.")
    @Email(message = "E-mail Inválido.")
    String email,

    @NotBlank(message = "Idade Obrigatória.")
    @Min(value = 16, message = "O estudante deve ter no mínimo 16 anos.")
    Integer idade,

    String telefone
) {

}
