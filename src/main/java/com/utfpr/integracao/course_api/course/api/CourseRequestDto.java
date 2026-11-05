package com.utfpr.integracao.course_api.course.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseRequestDto(
        @Size(min = 5, max = 127)
        String nome,
        @Size(min = 1)
        Integer cargaHoraria,
        @NotBlank
        String categoria,
        @NotNull
        Boolean ativo
) {

}
