<h1 align="center"> Trabalho 2 - Integração de Sistemas </h1>

<p align="center">
  <a href="https://forthebadge.com">
    <img src="https://forthebadge.com/badges/made-with-java.svg" alt="Badge 2" />
  </a>
  <a href="https://forthebadge.com">
    <img src="https://forthebadge.com/api/badges/generate?panels=2&primaryLabel=BUILT+WITH&secondaryLabel=COFFEE&primaryBGColor=%237dc832&secondaryBGColor=%237b9759&primaryTextColor=%23FFFFFF&primaryFontSize=12&primaryFontWeight=600&primaryLetterSpacing=2&primaryFontFamily=Roboto&primaryTextTransform=uppercase&secondaryTextColor=%23FFFFFF&secondaryFontSize=12&secondaryFontWeight=900&secondaryLetterSpacing=2&secondaryFontFamily=Montserrat&secondaryTextTransform=uppercase&secondaryIcon=coffeescript&secondaryIconColor=%23FFFFFF&secondaryIconSize=16&secondaryIconPosition=left" alt="Badge 1" />
  </a>
</p>

API REST em Spring Boot para gerenciar cursos, alunos e matrículas, com persistência em banco H2.
## Como executar

Requisitos: Java 25

```bash
./mvnw spring-boot:run
```

No Windows:

```bash
mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Endpoints

### Cursos
- `POST /cursos`
- `GET /cursos`
- `GET /cursos/{id}`
- `PUT /cursos/{id}`
- `DELETE /cursos/{id}`

### Alunos
- `POST /alunos`
- `GET /alunos`
- `GET /alunos/{id}`
- `PUT /alunos/{id}`
- `DELETE /alunos/{id}`

### Matrículas
- `POST /matriculas`
- `GET /matriculas`
- `GET /matriculas/{id}`
- `PUT /matriculas/{id}`
- `DELETE /matriculas/{id}`

### Detalhamento dos endpoints
- Disponível em `http://localhost:8080/swagger-ui.html` detalhando os objetos de envio e retorno, porém sem documentação

As listagesn aceitam paginação: `GET /cursos?page=0&size=5&sort=nome,asc`

## Testes

Alguns testes "de comportamento" feito com IA:

```bash
./test_api.sh
```
