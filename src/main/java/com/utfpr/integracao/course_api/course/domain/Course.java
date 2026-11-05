package com.utfpr.integracao.course_api.course.domain;

import java.util.List;

import com.utfpr.integracao.course_api.registration.domain.Registration;

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

    private Boolean ativo;

    private List<Registration> matriculas;
}
