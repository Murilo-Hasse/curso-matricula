package com.utfpr.integracao.course_api.student.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.utfpr.integracao.course_api.student.api.dto.StudentRequestDto;
import com.utfpr.integracao.course_api.student.api.dto.StudentResponseDto;
import com.utfpr.integracao.course_api.student.api.mapper.StudentApiMapper;
import com.utfpr.integracao.course_api.student.data.StudentEntity;
import com.utfpr.integracao.course_api.student.domain.StudentService;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/students")
public class StudentController {

    private StudentService service;
    private StudentApiMapper mapper;

    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> findAll(Pageable pageable) {
        List<StudentResponseDto> allStudents = this.mapper.toDto(this.service.getAll());

        return ResponseEntity.ok(allStudents);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<StudentResponseDto> findById(@PathVariable UUID id) {
//
//        return ResponseEntity.ok(new StudentResponseDto());
//    }

    @PostMapping
    public ResponseEntity<StudentResponseDto> save(@Valid @RequestBody StudentRequestDto studentRequestDto) {
        StudentEntity createdStudent = this.service.create(this.mapper.toEntity(studentRequestDto));

        StudentResponseDto createdStudentResponseDto = this.mapper.toDto(createdStudent);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdStudentResponseDto.id())
                .toUri();

        return ResponseEntity.created(location).body(createdStudentResponseDto);
    }

//    @PutMapping("/{id}")
//    public ResponseEntity<StudentResponseDto> update(@PathVariable UUID id, @RequestBody StudentRequestDto studentRequestDto) {
//
//        URI location = ServletUriComponentsBuilder
//                .fromCurrentRequest()
//                .path("/{id}")
//                .buildAndExpand(id)
//                .toUri();
//        return ResponseEntity.created(location).body(new StudentResponseDto());
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable UUID id) {
//        return ResponseEntity.noContent().build();
//    }
}
