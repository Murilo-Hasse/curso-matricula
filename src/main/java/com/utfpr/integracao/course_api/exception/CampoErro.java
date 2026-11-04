package com.utfpr.integracao.course_api.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

// classe que representa um campo de erro do bean validation
// resolvi criar para deixar mais bonitinho as mensagens dele
@AllArgsConstructor
@Data
public class CampoErro {

    String campo;

    String mensagem;
}
