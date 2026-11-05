package com.utfpr.integracao.course_api.student.api;

import com.utfpr.integracao.course_api.student.domain.Student;
import org.springframework.data.domain.Page;

public class StudentMapper {

    public static StudentResponseDto toResponseDto(Student student) {

        return new StudentResponseDto(
                student.getId(),
                student.getNome(),
                student.getEmail(),
                student.getIdade(),
                student.getTelefone()
        );
    }

    public static Student toDomain(StudentRequestDto requestDto, Long id) {

        return Student.builder()
                .id(id)
                .nome(requestDto.nome())
                .email(requestDto.email())
                .idade(requestDto.idade())
                .telefone(requestDto.telefone())
                .build();
    }

    public static Student toDomain(StudentRequestDto requestDto) {

        return toDomain(requestDto, null);
    }

    public static Page<StudentResponseDto> toResponseDto(Page<Student> domain) {

        return domain.map(StudentMapper::toResponseDto);
    }
}
