package com.utfpr.integracao.course_api.course.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequestDto(
        @NotBlank(message = "Nome obrigatório.")
        @Size(min = 5, max = 127, message = "O nome deve ter entre 5 e 127 caracteres.")
        String nome,

        @NotNull(message = "Carga horária obrigatória.")
        @Min(value = 1, message = "A carga horária deve ser de no mínimo 1 hora.")
        Integer cargaHoraria,

        @NotBlank(message = "Categoria obrigatória.")
        String categoria,

        @NotNull(message = "Status de ativo obrigatório.")
        Boolean ativo
) {

}
