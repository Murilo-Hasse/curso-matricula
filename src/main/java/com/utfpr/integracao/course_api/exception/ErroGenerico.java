package com.utfpr.integracao.course_api.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ErroGenerico {
    String titulo;

    Integer status;

    String mensagem;
}
