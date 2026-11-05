package com.utfpr.integracao.course_api.registration.api;

import java.time.LocalDate;

public record RegistrationResponseDto(
        Long id,
        LocalDate dataMatricula,
        String status,
        Long aluno,
        Long curso
) {

}
