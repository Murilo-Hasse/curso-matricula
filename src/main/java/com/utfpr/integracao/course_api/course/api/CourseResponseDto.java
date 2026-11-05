package com.utfpr.integracao.course_api.course.api;

public record CourseResponseDto(
        Long id,
        String nome,
        Integer cargaHoraria,
        String categoria,
        Boolean ativo
) {

}
