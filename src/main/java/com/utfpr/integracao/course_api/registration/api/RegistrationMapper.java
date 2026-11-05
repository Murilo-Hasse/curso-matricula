package com.utfpr.integracao.course_api.registration.api;

import com.utfpr.integracao.course_api.course.domain.Course;
import com.utfpr.integracao.course_api.registration.domain.Registration;
import com.utfpr.integracao.course_api.student.domain.Student;
import org.springframework.data.domain.Page;

public class RegistrationMapper {

    public static RegistrationResponseDto toResponseDto(Registration registration) {

        return new RegistrationResponseDto(
                registration.getId(),
                registration.getDataMatricula(),
                registration.getStatus(),
                registration.getAluno().getId(),
                registration.getCurso().getId()
        );
    }

    public static Registration toDomain(RegistrationRequestDto requestDto, Long id) {

        return Registration.builder()
                .id(id)
                .dataMatricula(requestDto.dataMatricula())
                .status(requestDto.status())
                .aluno(Student.builder().id(requestDto.aluno()).build())
                .curso(Course.builder().id(requestDto.curso()).build())
                .build();
    }

    public static Registration toDomain(RegistrationRequestDto requestDto) {

        return toDomain(requestDto, null);
    }

    public static Page<RegistrationResponseDto> toResponseDto(Page<Registration> domain) {

        return domain.map(RegistrationMapper::toResponseDto);
    }
}
