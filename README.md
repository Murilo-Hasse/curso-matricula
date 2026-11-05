# Trabalho 2 - Integração de Software
## Descrição

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

As listagesn aceitam paginação: `GET /cursos?page=0&size=5&sort=nome,asc`

## Testes

Alguns testes "de comportamento" feito com IA:

```bash
./test_api.sh
```