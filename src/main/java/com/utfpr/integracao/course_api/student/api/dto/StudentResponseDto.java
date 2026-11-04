package com.utfpr.integracao.course_api.student.api.dto;

public record StudentResponseDto(
        Long id,
        String nome,
        String email,
        Integer idade,
        String telefone
) {

}
