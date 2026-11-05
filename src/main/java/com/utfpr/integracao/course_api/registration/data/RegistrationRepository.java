package com.utfpr.integracao.course_api.registration.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {

    boolean existsByCursoId(Long cursoId);

    boolean existsByAlunoId(Long alunoId);
}
