package com.utfpr.integracao.course_api.registration.data;

import com.utfpr.integracao.course_api.course.data.CourseMapper;
import com.utfpr.integracao.course_api.registration.domain.Registration;
import com.utfpr.integracao.course_api.student.data.StudentMapper;
import org.springframework.data.domain.Page;

public class RegistrationMapper {

    public static RegistrationEntity toEntity(Registration registration){

        return RegistrationEntity.builder()
                .id(registration.getId())
                .dataMatricula(registration.getDataMatricula())
                .status(registration.getStatus())
                .curso(CourseMapper.toEntity(registration.getCurso()))
                .aluno(StudentMapper.toEntity(registration.getAluno()))
                .build();
    }

    public static Registration toDomain(RegistrationEntity entity){

        return Registration.builder()
                .id(entity.getId())
                .dataMatricula(entity.getDataMatricula())
                .status(entity.getStatus())
                .curso(CourseMapper.toDomain(entity.getCurso()))
                .aluno(StudentMapper.toDomain(entity.getAluno()))
                .build();
    }

    public static Page<Registration> toDomain(Page<RegistrationEntity> entity){
        return entity.map(RegistrationMapper::toDomain);
    }
}
