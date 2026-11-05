package com.utfpr.integracao.course_api.course.domain;

import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Course {

    private Long id;

    private String nome;

    private Integer cargaHoraria;

    private String categoria;

    private Boolean ativo = false;
}
