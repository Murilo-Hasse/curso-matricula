package com.utfpr.integracao.course_api.student.domain;

import com.utfpr.integracao.course_api.exception.AlunoNaoEncontradoException;
import com.utfpr.integracao.course_api.exception.ExclusaoAlunoComMatriculaException;
import com.utfpr.integracao.course_api.registration.data.RegistrationRepository;
import com.utfpr.integracao.course_api.student.data.StudentMapper;
import com.utfpr.integracao.course_api.student.data.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;

    private final RegistrationRepository registrationRepository;

    public Page<Student> findAll(Pageable pageable) {

        return StudentMapper.toDomain(repository.findAll(pageable));
    }

    public Student getById(Long id) {

        return repository.findById(id)
                .map(StudentMapper::toDomain)
                .orElseThrow(() -> new AlunoNaoEncontradoException(
                        String.format("Aluno com id %s não foi encontrado", id)));
    }

    public Student save(Student student) {

        return StudentMapper.toDomain(repository.save(StudentMapper.toEntity(student)));
    }

    public Student updateById(Long id, Student studentUpdObj) {

        validateById(id); //Validacao + pegar o que está no banco

        return save(studentUpdObj);
    }

    public void deleteById(Long id) {

        validateById(id); // Para ter validacao

        if (registrationRepository.existsByAlunoId(id)) {
            throw new ExclusaoAlunoComMatriculaException("Não é possível deletar aluno com matriculas ativas");
        }

        repository.deleteById(id);
    }

    private void validateById(Long id) {

        getById(id);
    }
}
