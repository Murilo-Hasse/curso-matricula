package com.utfpr.integracao.course_api.course.domain;

import java.util.List;

import com.utfpr.integracao.course_api.course.data.CourseEntity;
import com.utfpr.integracao.course_api.course.data.CourseMapper;
import com.utfpr.integracao.course_api.course.data.CourseRepository;
import com.utfpr.integracao.course_api.exception.CourseNotFound;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository repository;

    public Course save(Course course) {

        return CourseMapper.toDomain(repository.save(CourseMapper.toEntity(course)));
    }

    public Course findById(Long id) {

        return repository.findById(id)
                .map(CourseMapper::toDomain)
                .orElseThrow(() -> new CourseNotFound(
                        String.format("Curso com id %s não foi encontrado", id)));
    }

    public Page<Course> findAll(Pageable pageable) {

        return CourseMapper.toDomain(repository.findAll(pageable));
    }

    public void delete(Long id) {

        repository.deleteById(id);
    }
}
