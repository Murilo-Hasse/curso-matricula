package com.utfpr.integracao.course_api.student.api;

import java.net.URI;

import com.utfpr.integracao.course_api.student.api.dto.StudentRequestDto;
import com.utfpr.integracao.course_api.student.api.dto.StudentResponseDto;
import com.utfpr.integracao.course_api.student.api.mapper.StudentApiMapper;
import com.utfpr.integracao.course_api.student.data.Student;
import com.utfpr.integracao.course_api.student.domain.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
@RestController
@RequestMapping("/alunos")
public class StudentController {

    private final StudentService service;
    private final StudentApiMapper mapper;

    @GetMapping
    public ResponseEntity<Page<StudentResponseDto>> findAll(Pageable pageable) {
        Page<StudentResponseDto> allStudents = this.mapper.toDto(this.service.getAll(pageable));

        return ResponseEntity.ok(allStudents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> findById(@PathVariable Long id) {

        return ResponseEntity.ok(this.mapper.toDto(this.service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<StudentResponseDto> save(@Valid @RequestBody StudentRequestDto studentRequestDto) {
        Student createdStudent = this.service.create(this.mapper.toEntity(studentRequestDto));

        StudentResponseDto createdStudentResponseDto = this.mapper.toDto(createdStudent);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdStudentResponseDto.id())
                .toUri();

        return ResponseEntity.created(location).body(createdStudentResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> update(@PathVariable Long id, @Valid @RequestBody StudentRequestDto studentRequestDto) {

        Student updatedStudent = this.service.updateById(id, this.mapper.toEntity(studentRequestDto));

        return ResponseEntity.ok(this.mapper.toDto(updatedStudent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        this.service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}