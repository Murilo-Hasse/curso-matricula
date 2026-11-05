package com.utfpr.integracao.course_api.registration.domain;

import com.utfpr.integracao.course_api.course.data.CourseRepository;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationRequestDto;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationResponseDto;
import com.utfpr.integracao.course_api.registration.api.mapper.RegistrationApiMapper;
import com.utfpr.integracao.course_api.registration.data.Registration;
import com.utfpr.integracao.course_api.registration.repository.RegistrationRepository;
import com.utfpr.integracao.course_api.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository repository;
    private final RegistrationApiMapper mapper;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public RegistrationResponseDto create(RegistrationRequestDto dto) {

        return this.repository.save(entity);
    }
}
