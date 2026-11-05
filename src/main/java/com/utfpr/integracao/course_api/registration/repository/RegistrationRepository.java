package com.utfpr.integracao.course_api.registration.repository;

import com.utfpr.integracao.course_api.registration.data.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> { }
