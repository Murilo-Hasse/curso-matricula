package com.utfpr.integracao.course_api.registration.api;

import java.net.URI;

import com.utfpr.integracao.course_api.registration.domain.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
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

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/matriculas")
public class RegistrationController {

    private final RegistrationService service;

    @GetMapping
    public ResponseEntity<Page<RegistrationResponseDto>> findAll(Pageable pageable) {

        return ResponseEntity.ok(RegistrationMapper.toResponseDto(service.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> findById(@PathVariable Long id) {

        return ResponseEntity.ok(RegistrationMapper.toResponseDto(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<RegistrationResponseDto> save(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {

        var response = RegistrationMapper.toResponseDto(service.save(RegistrationMapper.toDomain(registrationRequestDto)));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> update(@PathVariable Long id, @Valid @RequestBody RegistrationRequestDto registrationRequestDto) {

        return ResponseEntity.ok(RegistrationMapper.toResponseDto(service.updateById(id, RegistrationMapper.toDomain(registrationRequestDto, id))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
