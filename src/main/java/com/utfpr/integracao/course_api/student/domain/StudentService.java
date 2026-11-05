package com.utfpr.integracao.course_api.student.domain;

import com.utfpr.integracao.course_api.student.data.StudentEntity;
import com.utfpr.integracao.course_api.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private StudentRepository repository;

    public List<StudentEntity> getAll() {

        return this.repository.findAll();
    }

    public StudentEntity create(StudentEntity entity) {

        return this.repository.save(entity);
    }
}
