package com.utfpr.integracao.course_api.student.data;

import com.utfpr.integracao.course_api.student.domain.Student;
import org.springframework.data.domain.Page;

public class StudentMapper {

    public static StudentEntity toEntity(Student student) {
        return StudentEntity.builder()
                .id(student.getId())
                .nome(student.getNome())
                .email(student.getEmail())
                .idade(student.getIdade())
                .telefone(student.getTelefone())
                .matriculas(student.getMatriculas())
                .build();
    }

    public static Student toDomain(StudentEntity entity) {
        return Student.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .email(entity.getEmail())
                .idade(entity.getIdade())
                .telefone(entity.getTelefone())
                .matriculas(entity.getMatriculas())
                .build();
    }

    public static Page<Student> toDomain(Page<StudentEntity> entities) {
        return entities.map(StudentMapper::toDomain);
    }
}
