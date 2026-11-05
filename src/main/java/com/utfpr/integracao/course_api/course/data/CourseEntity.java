package com.utfpr.integracao.course_api.course.data;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@Entity
@Table(name = "courses")
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 127,
            check = @CheckConstraint(
                    name = "check_nome_min",
                    constraint = "LENGTH(TRIM(nome)) >= 5"
            )
    )
    private String nome;

    @Column(name = "carga_horaria", nullable = false,
        check = @CheckConstraint(
                name = "check_ch_min",
                constraint = "carga_horaria > 0"
        )
    )
    private Integer cargaHoraria;

    @Column(name = "categoria", nullable = false, length = 127)
    private String categoria;

    @Column(name = "active", nullable = false)
    private Boolean ativo = false;
}
