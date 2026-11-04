package com.utfpr.integracao.course_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

public class GlobalExceptionHandler {
    // responsavel por deixar a mensagem do beanvalidator mais biontinha
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroValidacao> tratarErroBeanValidator(MethodArgumentNotValidException ex,
                                                                 HttpServletRequest request) {

        List<CampoErro> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map((error) -> new CampoErro(error.getField(), error.getDefaultMessage()))
                .toList();

        ErroValidacao erro = new ErroValidacao(HttpStatus.BAD_REQUEST.value(),
                "Dados errados. Verifique os campos informados.", request.getRequestURI(), errors);

        return ResponseEntity.badRequest().body(erro);
    }
}
