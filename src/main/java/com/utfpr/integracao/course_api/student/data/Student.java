package com.utfpr.integracao.course_api.student.data;

import com.utfpr.integracao.course_api.registration.data.Registration;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Integer idade;

    private String telefone;

    @OneToMany(mappedBy = "aluno")
    private List<Registration> matriculas;
}
