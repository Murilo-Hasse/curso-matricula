package com.utfpr.integracao.course_api.course.api;

import com.utfpr.integracao.course_api.course.domain.Course;
import org.springframework.data.domain.Page;

public class CourseMapper {

    public static CourseResponseDto toResponseDto(Course course) {

        return new CourseResponseDto(course.getId(), course.getNome(), course.getCargaHoraria(), course.getCategoria(), course.getAtivo());
    }

    public static Course toDomain(CourseRequestDto requestDto, Long id) {
        return Course.builder()
                .id(id)
                .nome(requestDto.nome())
                .cargaHoraria(requestDto.cargaHoraria())
                .categoria(requestDto.categoria())
                .ativo(requestDto.ativo())
                .build();
    }

    public static Course toDomain(CourseRequestDto requestDto) {

        return toDomain(requestDto, null);
    }

    public static Page<CourseResponseDto> toResponseDto(Page<Course> domain) {
        return domain.map(CourseMapper::toResponseDto);
    }
}
