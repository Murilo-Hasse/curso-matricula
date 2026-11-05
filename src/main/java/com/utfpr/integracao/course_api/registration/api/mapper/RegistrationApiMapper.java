package com.utfpr.integracao.course_api.registration.api.mapper;

import com.utfpr.integracao.course_api.course.data.CourseEntity;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationRequestDto;
import com.utfpr.integracao.course_api.registration.data.Registration;
import com.utfpr.integracao.course_api.student.data.Student;
import org.springframework.stereotype.Component;

@Component
public class RegistrationApiMapper {
    public Registration toEntity(RegistrationRequestDto dto, Student student, CourseEntity course) {

        return Registration.builder()
                .dataMatricula(dto.dataMatricula())
                .status(dto.status())
                .aluno(student)
                .curso(course)
                .build();
    }
}
