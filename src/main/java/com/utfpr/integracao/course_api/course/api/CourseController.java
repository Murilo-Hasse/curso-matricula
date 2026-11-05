package com.utfpr.integracao.course_api.course.api;

import java.net.URI;
import java.util.List;

import com.utfpr.integracao.course_api.course.domain.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService service;

    @GetMapping
    public ResponseEntity<Page<CourseResponseDto>> findAll(Pageable pageable) {

        return ResponseEntity.ok(CourseMapper.toResponseDto(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseResponseDto> findById(@PathVariable Long id) {

        return ResponseEntity.ok(CourseMapper.toResponseDto(service.findById(id)));
    }

    @PostMapping
    public ResponseEntity<CourseResponseDto> save(@Valid @RequestBody CourseRequestDto courseRequestDto) {

        Long id = 0L;
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(CourseMapper.toResponseDto(service.save(CourseMapper.toDomain(courseRequestDto))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponseDto> update(@PathVariable Long id,
                                                     @RequestBody CourseRequestDto courseRequestDto) {
//
        return null;
//        URI location = ServletUriComponentsBuilder
//                .fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(id)
//                .toUri();
//        return ResponseEntity.created(location).body(new CourseResponseDto());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
