package com.utfpr.integracao.course_api.registration.data;

import com.utfpr.integracao.course_api.course.domain.Course;
import com.utfpr.integracao.course_api.student.data.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dataMatricula;

    private String status;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student aluno;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course curso;
}
