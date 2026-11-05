package com.utfpr.integracao.course_api.registration.domain;

import com.utfpr.integracao.course_api.exception.MatriculaEmCursoInativoException;
import com.utfpr.integracao.course_api.exception.MatriculaNaoEncontradaException;
import com.utfpr.integracao.course_api.registration.data.Registration;
import com.utfpr.integracao.course_api.registration.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository repository;

    public Page<Registration> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Registration create(Registration entity) {

        if(entity.getCurso().getAtivo()) {
            throw new MatriculaEmCursoInativoException("Não é possível se matricular em um curso inativo");
        }
        //matricula < 16 anos n é necessário porque nem é possível criar aluno menor que 16

        return this.repository.save(entity);
    }

    public Registration getById(Long id) {

        return this.repository.findById(id)
                .orElseThrow(() -> new MatriculaNaoEncontradaException("Não foi encontrado uma matricula com o ID " + id));
    }


    public void deleteById(Long id) {
        Registration registration = this.getById(id); // Para ter validacao

        repository.delete(registration);
    }

    public Registration updateById(Long id, Registration registrationUpdateObj) {
        Registration registration = this.getById(id); //Validacao + pegar o que está no banco

        registration.setAluno(registrationUpdateObj.getAluno());
        registration.setCurso(registrationUpdateObj.getCurso());
        registration.setStatus(registrationUpdateObj.getStatus());
        registration.setDataMatricula(registrationUpdateObj.getDataMatricula());

        return this.repository.save(registration);
    }
}
