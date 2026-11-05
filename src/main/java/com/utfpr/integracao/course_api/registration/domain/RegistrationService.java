package com.utfpr.integracao.course_api.registration.domain;

import com.utfpr.integracao.course_api.course.domain.CourseService;
import com.utfpr.integracao.course_api.exception.MatriculaEmCursoInativoException;
import com.utfpr.integracao.course_api.exception.MatriculaNaoEncontradaException;
import com.utfpr.integracao.course_api.registration.data.RegistrationMapper;
import com.utfpr.integracao.course_api.registration.data.RegistrationRepository;
import com.utfpr.integracao.course_api.student.domain.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository repository;

    private final CourseService courseService;

    private final StudentService studentService;

    public Page<Registration> findAll(Pageable pageable) {

        return RegistrationMapper.toDomain(repository.findAll(pageable));
    }

    public Registration getById(Long id) {

        return repository.findById(id)
                .map(RegistrationMapper::toDomain)
                .orElseThrow(() -> new MatriculaNaoEncontradaException(
                        String.format("Matrícula com id %s não foi encontrada", id)));
    }

    public Registration save(Registration registration) {

        registration.setCurso(courseService.getById(registration.getCurso().getId()));
        registration.setAluno(studentService.getById(registration.getAluno().getId()));

        if (!registration.getCurso().getAtivo()) {
            throw new MatriculaEmCursoInativoException("Não é possível se matricular em um curso inativo");
        }
        //matricula < 16 anos n é necessário porque nem é possível criar aluno menor que 16

        return RegistrationMapper.toDomain(repository.save(RegistrationMapper.toEntity(registration)));
    }

    public Registration updateById(Long id, Registration registrationUpdObj) {

        validateById(id); //Validacao + pegar o que está no banco

        return save(registrationUpdObj);
    }

    public void deleteById(Long id) {

        validateById(id); // Para ter validacao

        repository.deleteById(id);
    }

    private void validateById(Long id) {

        getById(id);
    }
}
