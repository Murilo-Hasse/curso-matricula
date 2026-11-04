package com.utfpr.integracao.course_api.exception;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ErroValidacao {

    Integer status;

    String mensagem;

    String uri;

    List<CampoErro> erros;
}

