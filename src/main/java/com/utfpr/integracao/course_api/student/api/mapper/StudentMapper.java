package com.utfpr.integracao.course_api.student.api.mapper;

import com.utfpr.integracao.course_api.student.api.dto.StudentRequestDto;
import com.utfpr.integracao.course_api.student.api.dto.StudentResponseDto;
import com.utfpr.integracao.course_api.student.domain.Student;
import org.springframework.data.domain.Page;

public class StudentMapper {

    public static Student toDomain(StudentRequestDto dto) {

        return Student.builder()
                .nome(dto.nome())
                .email(dto.email())
                .idade(dto.idade())
                .telefone(dto.telefone())
                .build();
    }

    public static StudentResponseDto toDto(Student student) {

        return new StudentResponseDto(
                student.getId(),
                student.getNome(),
                student.getEmail(),
                student.getIdade(),
                student.getTelefone()
        );
    }

    public static Page<StudentResponseDto> toDto(Page<Student> domain) {

        return domain.map(StudentMapper::toDto);
    }
}
