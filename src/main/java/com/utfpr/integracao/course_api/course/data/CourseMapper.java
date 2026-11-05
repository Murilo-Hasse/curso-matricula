package com.utfpr.integracao.course_api.course.data;

import com.utfpr.integracao.course_api.course.domain.Course;
import org.springframework.data.domain.Page;

public class CourseMapper {

    public static CourseEntity toEntity(Course course) {

        return CourseEntity.builder()
                .id(course.getId())
                .nome(course.getNome())
                .cargaHoraria(course.getCargaHoraria())
                .categoria(course.getCategoria())
                .ativo(course.getAtivo())
                .build();
    }

    public static Course toDomain(CourseEntity entity) {

        return Course.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .cargaHoraria(entity.getCargaHoraria())
                .categoria(entity.getCategoria())
                .ativo(entity.getAtivo())
                .build();
    }

    public static Page<Course> toDomain(Page<CourseEntity> entities) {

        return entities.map(CourseMapper::toDomain);
    }
}
