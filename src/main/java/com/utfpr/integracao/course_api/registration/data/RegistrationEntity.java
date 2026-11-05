package com.utfpr.integracao.course_api.registration.data;

import com.utfpr.integracao.course_api.course.data.CourseEntity;
import com.utfpr.integracao.course_api.student.data.StudentEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataMatricula;

    private String status;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity aluno;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private CourseEntity curso;
}
