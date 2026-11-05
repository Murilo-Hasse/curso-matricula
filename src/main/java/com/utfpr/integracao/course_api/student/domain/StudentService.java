package com.utfpr.integracao.course_api.student.domain;

import com.utfpr.integracao.course_api.exception.AlunoNaoEncontradoException;
import com.utfpr.integracao.course_api.student.data.StudentMapper;
import com.utfpr.integracao.course_api.student.data.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;

    public Page<Student> findAll(Pageable pageable) {

        return StudentMapper.toDomain(repository.findAll(pageable));
    }

    public Student getById(Long id) {

        return repository.findById(id)
                .map(StudentMapper::toDomain)
                .orElseThrow( () -> new AlunoNaoEncontradoException(
                        String.format( "Não foi encontrado um aluno com o ID %s", id)));
    }

    public Student create(Student entity) {

        return StudentMapper.toDomain(repository.save(StudentMapper.toEntity(entity)));
    }

    public void deleteById(Long id) {
        Student student = this.getById(id); // Para ter validacao

        repository.delete(StudentMapper.toEntity(student));
    }

    public Student updateById(Long id, Student studentUpdObj) {
        getById(id); //Validacao + pegar o que está no banco

        return StudentMapper.toDomain(repository.save(StudentMapper.toEntity(studentUpdObj)));
    }
}
