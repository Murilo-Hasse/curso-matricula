package com.utfpr.integracao.course_api.student.api.mapper;

import com.utfpr.integracao.course_api.student.api.dto.StudentRequestDto;
import com.utfpr.integracao.course_api.student.api.dto.StudentResponseDto;
import com.utfpr.integracao.course_api.student.data.Student;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentApiMapper {

    public Student toEntity(StudentRequestDto dto) {

        return Student.builder()
                .nome(dto.nome())
                .email(dto.email())
                .idade(dto.idade())
                .telefone(dto.telefone())
                .build();
    }

    public StudentResponseDto toDto(Student student) {

        return new StudentResponseDto(
                student.getId(),
                student.getNome(),
                student.getEmail(),
                student.getIdade(),
                student.getTelefone()
        );
    }

    public Page<StudentResponseDto> toDto(Page<Student> domain) {

        return domain.map(this::toDto);
    }
}
