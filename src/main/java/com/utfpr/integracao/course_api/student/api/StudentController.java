package com.utfpr.integracao.course_api.student.api;

import java.net.URI;

import com.utfpr.integracao.course_api.student.domain.StudentService;
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
@RequestMapping("/alunos")
public class StudentController {

    private final StudentService service;

    @GetMapping
    public ResponseEntity<Page<StudentResponseDto>> findAll(Pageable pageable) {

        return ResponseEntity.ok(StudentMapper.toResponseDto(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> findById(@PathVariable Long id) {

        return ResponseEntity.ok(StudentMapper.toResponseDto(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<StudentResponseDto> save(@Valid @RequestBody StudentRequestDto studentRequestDto) {

        var response = StudentMapper.toResponseDto(service.save(StudentMapper.toDomain(studentRequestDto)));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> update(@PathVariable Long id, @Valid @RequestBody StudentRequestDto studentRequestDto) {

        return ResponseEntity.ok(StudentMapper.toResponseDto(service.updateById(id, StudentMapper.toDomain(studentRequestDto, id))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
