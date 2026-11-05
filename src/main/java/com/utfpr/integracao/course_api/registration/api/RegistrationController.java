package com.utfpr.integracao.course_api.registration.api;
import java.net.URI;
import java.util.List;

import com.utfpr.integracao.course_api.course.data.CourseEntity;
import com.utfpr.integracao.course_api.course.data.CourseMapper;
import com.utfpr.integracao.course_api.course.domain.CourseService;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationRequestDto;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationResponseDto;
import com.utfpr.integracao.course_api.registration.api.mapper.RegistrationApiMapper;
import com.utfpr.integracao.course_api.registration.data.Registration;
import com.utfpr.integracao.course_api.registration.domain.RegistrationService;
import com.utfpr.integracao.course_api.student.api.dto.StudentResponseDto;
import com.utfpr.integracao.course_api.student.data.Student;
import com.utfpr.integracao.course_api.student.domain.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@RestController
@RequestMapping("/matriculas")
public class RegistrationController {

    private final RegistrationService service;
    private final RegistrationApiMapper mapper;
    private final CourseService courseService;
    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<Page<RegistrationResponseDto>> findAll(Pageable pageable) {
        Page<RegistrationResponseDto> allRegistrations = mapper.toDto(service.getAll(pageable));

        return ResponseEntity.ok(allRegistrations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> findById(@PathVariable Long id) {

        return ResponseEntity.ok(this.mapper.toDto(this.service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<RegistrationResponseDto> save(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {

        Student student = studentService.getById(registrationRequestDto.aluno());
        CourseEntity course = CourseMapper.toEntity(courseService.getById(registrationRequestDto.curso()));

        Registration createdRegistration = service.create(mapper.toEntity(registrationRequestDto, student, course));
        RegistrationResponseDto responseDto = mapper.toDto(createdRegistration);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDto.id())
                .toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> update(@PathVariable Long id,
                                               @Valid @RequestBody RegistrationRequestDto registrationRequestDto) {

        Student student = studentService.getById(registrationRequestDto.aluno());
        CourseEntity course = CourseMapper.toEntity(courseService.getById(registrationRequestDto.curso()));

        Registration updatedRegistration = service.updateById(id, mapper.toEntity(registrationRequestDto, student, course));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(updatedRegistration.getId())
                .toUri();
        return ResponseEntity.created(location).body(mapper.toDto(updatedRegistration));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        this.service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
