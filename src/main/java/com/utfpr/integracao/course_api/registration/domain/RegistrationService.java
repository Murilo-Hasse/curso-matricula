package com.utfpr.integracao.course_api.registration.domain;

import com.utfpr.integracao.course_api.exception.MatriculaEmCursoInativoException;
import com.utfpr.integracao.course_api.registration.data.Registration;
import com.utfpr.integracao.course_api.registration.repository.RegistrationRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository repository;

    public Registration create(Registration entity) {

        if(entity.getCurso().getAtivo()) {
            throw new MatriculaEmCursoInativoException("Não é possível se matricular em um curso inativo");
        }
        //matricula < 16 anos n é necessário porque nem é possível criar aluno menor que 16

        return this.repository.save(entity);
    }
}
