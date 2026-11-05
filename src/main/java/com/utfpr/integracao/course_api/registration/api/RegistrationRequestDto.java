package com.utfpr.integracao.course_api.registration.api;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record RegistrationRequestDto(
        @FutureOrPresent(message = "A data da matrícula não pode ser no passado.")
        @NotNull(message = "Data da matrícula obrigatória.")
        LocalDate dataMatricula,

        @NotBlank(message = "Status da matrícula obrigatório")
        String status,

        @NotNull(message = "Id do aluno obrigatório.")
        @Positive(message = "O id do aluno não pode ser negativo.")
        Long aluno,

        @NotNull(message = "Id do curso obrigatório.")
        @Positive(message = "O id do curso não pode ser negativo.")
        Long curso
) {

}
