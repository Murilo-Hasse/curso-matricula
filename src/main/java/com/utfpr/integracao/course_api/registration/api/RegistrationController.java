package com.utfpr.integracao.course_api.registration.api;

import java.net.URI;

import com.utfpr.integracao.course_api.registration.api.dto.RegistrationRequestDto;
import com.utfpr.integracao.course_api.registration.api.dto.RegistrationResponseDto;
import com.utfpr.integracao.course_api.registration.api.mapper.RegistrationMapper;
import com.utfpr.integracao.course_api.registration.domain.Registration;
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

        Page<RegistrationResponseDto> allRegistrations = RegistrationMapper.toDto(service.getAll(pageable));

        return ResponseEntity.ok(allRegistrations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> findById(@PathVariable Long id) {

        return ResponseEntity.ok(RegistrationMapper.toDto(service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<RegistrationResponseDto> save(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {

        Registration createdRegistrationEntity = service.save(RegistrationMapper.toDomain(registrationRequestDto, null, null));
        RegistrationResponseDto responseDto = RegistrationMapper.toDto(createdRegistrationEntity);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDto.id())
                .toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> update(@PathVariable Long id, @Valid @RequestBody RegistrationRequestDto registrationRequestDto) {

        Registration updatedRegistration = service.updateById(id, RegistrationMapper.toDomain(registrationRequestDto, null, null));

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(updatedRegistration.getId())
                .toUri();
        return ResponseEntity.created(location).body(RegistrationMapper.toDto(updatedRegistration));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        this.service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
