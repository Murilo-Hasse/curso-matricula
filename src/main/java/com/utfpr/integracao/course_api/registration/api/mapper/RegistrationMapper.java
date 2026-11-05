package com.utfpr.integracao.course_api.registration.api.mapper;

import com.utfpr.integracao.course_api.course.domain.Course;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationRequestDto;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationResponseDto;
import com.utfpr.integracao.course_api.registration.domain.Registration;
import com.utfpr.integracao.course_api.student.domain.Student;
import org.springframework.data.domain.Page;

public class RegistrationMapper {

    public static Registration toDomain(RegistrationRequestDto dto, Student student, Course course) {

        return Registration.builder()
                .dataMatricula(dto.dataMatricula())
                .status(dto.status())
                .aluno(student)
                .curso(course)
                .build();
    }

    public static RegistrationResponseDto toDto(Registration registration) {

        return new RegistrationResponseDto(
                registration.getId(),
                registration.getDataMatricula(),
                registration.getStatus(),
                registration.getAluno().getId(),
                registration.getCurso().getId()
        );
    }

    public static Page<RegistrationResponseDto> toDto(Page<Registration> domain) {

        return domain.map(RegistrationMapper::toDto);
    }
}
