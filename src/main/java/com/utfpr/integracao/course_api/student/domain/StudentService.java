package com.utfpr.integracao.course_api.student.domain;

import com.utfpr.integracao.course_api.exception.AlunoNaoEncontradoException;
import com.utfpr.integracao.course_api.student.data.Student;
import com.utfpr.integracao.course_api.student.data.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;

    public Page<Student> getAll(Pageable pageable) {

        return this.repository.findAll(pageable);
    }

    public Student getById(Long id) {

        return this.repository.findById(id)
                .orElseThrow(() -> new AlunoNaoEncontradoException("Não foi encontrado um aluno com o ID " + id));
    }

    public Student create(Student entity) {

        return this.repository.save(entity);
    }

    public void deleteById(Long id) {
        Student student = this.getById(id); // Para ter validacao

        repository.delete(student);
    }

    public Student updateById(Long id, Student studentUpdObj) {
        Student student = this.getById(id); //Validacao + pegar o que está no banco

        student.setNome(studentUpdObj.getNome());
        student.setEmail(studentUpdObj.getEmail());
        student.setIdade(studentUpdObj.getIdade());
        student.setTelefone(studentUpdObj.getTelefone());

        return this.repository.save(student);
    }
}
