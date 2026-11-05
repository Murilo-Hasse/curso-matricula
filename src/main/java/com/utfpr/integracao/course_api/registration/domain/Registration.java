package com.utfpr.integracao.course_api.registration.domain;

import java.time.LocalDate;

import com.utfpr.integracao.course_api.course.domain.Course;
import com.utfpr.integracao.course_api.student.domain.Student;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Registration {

    private Long id;

    private LocalDate dataMatricula;

    private String status;

    private Student aluno;

    private Course curso;

}
