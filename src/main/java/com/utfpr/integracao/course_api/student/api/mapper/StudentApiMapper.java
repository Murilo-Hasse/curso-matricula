package com.utfpr.integracao.course_api.student.api.mapper;

import com.utfpr.integracao.course_api.student.api.dto.StudentRequestDto;
import com.utfpr.integracao.course_api.student.api.dto.StudentResponseDto;
import com.utfpr.integracao.course_api.student.data.StudentEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StudentApiMapper {

    public StudentEntity toEntity(StudentRequestDto dto) {

        return StudentEntity.builder()
                .nome(dto.nome())
                .email(dto.email())
                .idade(dto.idade())
                .telefone(dto.telefone())
                .build();
    }

    public StudentResponseDto toDto(StudentEntity studentEntity) {

        return new StudentResponseDto(
                studentEntity.getId(),
                studentEntity.getNome(),
                studentEntity.getEmail(),
                studentEntity.getIdade(),
                studentEntity.getTelefone()
        );
    }

    public List<StudentResponseDto> toDto(List<StudentEntity> domain) {

        return domain.stream().map(this::toDto).collect(Collectors.toList());
    }
}
