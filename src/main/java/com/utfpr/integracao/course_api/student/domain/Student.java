package com.utfpr.integracao.course_api.student.domain;

import java.util.List;

import com.utfpr.integracao.course_api.registration.data.RegistrationEntity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Student {

    private Long id;

    private String nome;

    private String email;

    private Integer idade;

    private String telefone;

    private List<RegistrationEntity> matriculas;
}
