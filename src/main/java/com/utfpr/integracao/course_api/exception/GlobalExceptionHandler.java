package com.utfpr.integracao.course_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AlunoNaoEncontradoException.class)
    public ResponseEntity<ErroNaoEncontrado> tratarLivroNaoEncontrado(AlunoNaoEncontradoException ex,
                                                                      HttpServletRequest request) {

        ErroNaoEncontrado error = new ErroNaoEncontrado("Aluno não encontrado", HttpStatus.NOT_FOUND.value(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

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
