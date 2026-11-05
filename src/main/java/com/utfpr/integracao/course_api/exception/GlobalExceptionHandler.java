package com.utfpr.integracao.course_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AlunoNaoEncontradoException.class)
    public ResponseEntity<ErroGenerico> tratarAlunoNaoEncontrado(AlunoNaoEncontradoException ex,
                                                                 HttpServletRequest request) {

        ErroGenerico error = new ErroGenerico("Aluno não encontrado", HttpStatus.NOT_FOUND.value(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(CursoNaoEncontradoException.class)
    public ResponseEntity<ErroGenerico> tratarCursoNaoEncontrado(CursoNaoEncontradoException ex,
                                                                 HttpServletRequest request) {

        ErroGenerico error = new ErroGenerico("Curso não encontrado", HttpStatus.NOT_FOUND.value(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MatriculaNaoEncontradaException.class)
    public ResponseEntity<ErroGenerico> tratarMatriculaNaoEncontrada(MatriculaNaoEncontradaException ex,
                                                                 HttpServletRequest request) {

        ErroGenerico error = new ErroGenerico("Matrícula não encontrada", HttpStatus.NOT_FOUND.value(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MatriculaEmCursoInativoException.class)
    public ResponseEntity<ErroGenerico> tratarMatriculaEmCursoInativo(MatriculaEmCursoInativoException ex,
                                                                      HttpServletRequest request) {

        ErroGenerico error = new ErroGenerico("Não é possível realizar matricula em curso inativo.", HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ExclusaoAlunoComMatriculaException.class)
    public ResponseEntity<ErroGenerico> tratarExclusaoAlunoComMatricula(ExclusaoAlunoComMatriculaException ex,
                                                                      HttpServletRequest request) {

        ErroGenerico error = new ErroGenerico("Não é possível deletar um usuário com matriculas ativas.", HttpStatus.BAD_REQUEST.value(),
                ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ExclusaoCursoComMatriculaException.class)
    public ResponseEntity<ErroGenerico> tratarExclusaoCursoComMatricula(ExclusaoCursoComMatriculaException ex,
                                                                      HttpServletRequest request) {

        ErroGenerico error = new ErroGenerico("Não é possível deletar um curso com matriculas de alunos ativas.", HttpStatus.BAD_REQUEST.value(),
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
