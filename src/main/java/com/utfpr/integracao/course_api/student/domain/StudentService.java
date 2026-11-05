package com.utfpr.integracao.course_api.student.domain;

import com.utfpr.integracao.course_api.student.data.Student;
import com.utfpr.integracao.course_api.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;

    public Page<Student> getAll(Pageable pageable) {

        return this.repository.findAll(pageable);
    }

    public Student create(Student entity) {

        return this.repository.save(entity);
    }
}
