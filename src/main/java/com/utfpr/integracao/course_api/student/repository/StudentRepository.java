package com.utfpr.integracao.course_api.student.repository;

import com.utfpr.integracao.course_api.student.data.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<StudentEntity, Long> { }
