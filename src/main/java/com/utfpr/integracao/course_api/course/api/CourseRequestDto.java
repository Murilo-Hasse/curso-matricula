package com.utfpr.integracao.course_api.course.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequestDto(
        @Size(min = 5, max = 127, message = "O nome deve ter entre 5 e 127 caracteres.")
        String nome,
        @Min(value = 1)
        Integer cargaHoraria,
        @NotBlank
        String categoria,
        @NotNull
        Boolean ativo
) {

}
