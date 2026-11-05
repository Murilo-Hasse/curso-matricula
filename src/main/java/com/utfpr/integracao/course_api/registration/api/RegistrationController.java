package com.utfpr.integracao.course_api.registration.api;
import java.net.URI;
import java.util.List;

import com.utfpr.integracao.course_api.registration.api.dto.RegistrationRequestDto;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationResponseDto;
import com.utfpr.integracao.course_api.registration.data.Registration;
import com.utfpr.integracao.course_api.registration.domain.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RequiredArgsConstructor
@RestController
@RequestMapping("/matriculas")
public class RegistrationController {

    private final RegistrationService service;

    @GetMapping
    public ResponseEntity<List<RegistrationResponseDto>> findAll(Pageable pageable) {

        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> findById(@PathVariable Long id) {

        return ResponseEntity.ok();
    }

    @PostMapping
    public ResponseEntity<RegistrationResponseDto> save(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {

        RegistrationResponseDto createdRegistration = this.service.create(registrationRequestDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRegistration.id())
                .toUri();
        return ResponseEntity.created(location).body(createdRegistration);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> update(@PathVariable Long id,
                                               @RequestBody RegistrationRequestDto registrationRequestDto) {

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).body(new RegistrationResponseDto());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}
