package com.utfpr.integracao.course_api.student.domain;

import com.utfpr.integracao.course_api.student.data.Student;
import com.utfpr.integracao.course_api.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;

    public List<Student> getAll() {

        return this.repository.findAll();
    }

    public Student create(Student entity) {

        return this.repository.save(entity);
    }
}
