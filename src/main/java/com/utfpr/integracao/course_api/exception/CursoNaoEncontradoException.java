package com.utfpr.integracao.course_api.exception;

public class CursoNaoEncontradoException extends RuntimeException {
    public CursoNaoEncontradoException(String message) {
        super(message);
    }
}
