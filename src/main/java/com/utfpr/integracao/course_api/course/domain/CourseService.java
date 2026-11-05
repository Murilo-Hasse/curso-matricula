package com.utfpr.integracao.course_api.course.domain;

import com.utfpr.integracao.course_api.course.data.CourseMapper;
import com.utfpr.integracao.course_api.course.data.CourseRepository;
import com.utfpr.integracao.course_api.exception.CursoNaoEncontradoException;
import com.utfpr.integracao.course_api.exception.ExclusaoCursoComMatriculaException;
import com.utfpr.integracao.course_api.registration.data.RegistrationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository repository;

    private final RegistrationRepository registrationRepository;

    public Page<Course> findAll(Pageable pageable) {

        return CourseMapper.toDomain(repository.findAll(pageable));
    }

    /***
     *
     * @param id from database
     * @return Course domain object
     * @throws CursoNaoEncontradoException exeption when course not found
     */
    public Course getById(Long id) {

        return repository.findById(id)
                .map(CourseMapper::toDomain)
                .orElseThrow(() -> new CursoNaoEncontradoException(
                        String.format("Curso com id %s não foi encontrado", id)));
    }

    public Course save(Course course) {

        return CourseMapper.toDomain(repository.save(CourseMapper.toEntity(course)));
    }

    public Course updateById(Long id, Course courseUpdObj) {

        validateById(id); //Validacao + pegar o que está no banco

        return save(courseUpdObj);
    }

    public void deleteById(Long id) {

        validateById(id); // Para ter validacao

        if (registrationRepository.existsByCursoId(id)) {
            throw new ExclusaoCursoComMatriculaException("Não é possível deletar curso com matriculas ativas");
        }

        repository.deleteById(id);
    }

    private void validateById(Long id) {

        getById(id);
    }
}
