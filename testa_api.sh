#!/usr/bin/env bash

# ==============================================================================
# Script de Teste Automatizado da API REST - Gerenciamento de Cursos e Matrículas
# ==============================================================================
# Uso:  ./testa_api.sh                          (usa http://localhost:8080)
#       BASE_URL=http://localhost:8081 ./testa_api.sh
#
# Cobre:
#   1. CRUD de Cursos      (POST/GET/GET{id}/PUT/DELETE /cursos)
#   2. CRUD de Alunos      (POST/GET/GET{id}/PUT/DELETE /alunos)
#   3. CRUD de Matrículas  (POST/GET/GET{id}/PUT/DELETE /matriculas)
#   4. Paginação           (page, size, sort)
#   5. Bean Validation     (@NotBlank, @NotNull, @Size, @Email, @Min, @Positive, ...)
#   6. Requisitos REST     (status codes, header Location, 404 em recurso inexistente)
#   7. Regras de Negócio   (curso inativo, aluno < 16, exclusão com matrículas)
# ==============================================================================

BASE_URL="${BASE_URL:-http://localhost:8080}"

# Datas dinâmicas (dataMatricula usa @FutureOrPresent)
HOJE=$(date +%F)
ONTEM=$(date -d "yesterday" +%F)
AMANHA=$(date -d "tomorrow" +%F)

# Sufixo único para permitir rodar o script várias vezes na mesma instância
RUN_ID=$(date +%s)

# Cores para formatação de output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

PASS=0
FAIL=0
FAILED_TESTS=()

echo -e "${BLUE}======================================================================${NC}"
echo -e "${BLUE}   Iniciando bateria de testes na API REST ($BASE_URL)${NC}"
echo -e "${BLUE}======================================================================${NC}"

# Verifica se o jq está instalado
if ! command -v jq &> /dev/null; then
    echo -e "${RED}Erro: 'jq' não está instalado. Instale-o com 'sudo apt install jq' ou similar.${NC}"
    exit 1
fi

# Verifica se a API está no ar
if ! curl -s -o /dev/null "$BASE_URL/cursos"; then
    echo -e "${RED}Erro: API não está respondendo em $BASE_URL${NC}"
    exit 1
fi

TMP_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_DIR"' EXIT

# ------------------------------------------------------------------------------
# Funções auxiliares
# ------------------------------------------------------------------------------

# Executa uma requisição e popula HTTP_CODE, BODY e HEADERS
# Uso: request METODO /caminho ['json']
request() {
    local method=$1
    local path=$2
    local data=$3
    local args=(-s -X "$method" "$BASE_URL$path" -D "$TMP_DIR/headers" -o "$TMP_DIR/body" -w "%{http_code}")

    if [ -n "$data" ]; then
        args+=(-H "Content-Type: application/json" -d "$data")
    fi

    HTTP_CODE=$(curl "${args[@]}")
    BODY=$(cat "$TMP_DIR/body")
    HEADERS=$(tr -d '\r' < "$TMP_DIR/headers")
}

pass() {
    PASS=$((PASS + 1))
    echo -e "[ ${GREEN}OK${NC} ] $1"
}

fail() {
    FAIL=$((FAIL + 1))
    FAILED_TESTS+=("$1")
    echo -e "[ ${RED}FAIL${NC} ] $1"
    if [ -n "$2" ]; then
        echo -e "         ${RED}↳ $2${NC}"
    fi
}

# Valida o status HTTP. Aceita mais de um status esperado: check_status "Nome" 400 422
check_status() {
    local test_name=$1
    shift
    local expected
    for expected in "$@"; do
        if [ "$HTTP_CODE" = "$expected" ]; then
            pass "$test_name (Status: $HTTP_CODE)"
            return 0
        fi
    done
    fail "$test_name (Esperado: $*, Recebido: $HTTP_CODE)" "$(echo "$BODY" | head -c 300)"
    return 1
}

# Valida uma expressão jq (deve resultar em true) sobre o BODY atual
check_json() {
    local test_name=$1
    local filter=$2
    if echo "$BODY" | jq -e "$filter" > /dev/null 2>&1; then
        pass "$test_name"
    else
        fail "$test_name" "Filtro jq não satisfeito: $filter | Body: $(echo "$BODY" | head -c 300)"
    fi
}

# Valida que a resposta de erro de Bean Validation aponta o campo esperado
check_validation_field() {
    local test_name=$1
    local campo=$2
    check_json "$test_name -> erro aponta campo '$campo' com mensagem" \
        "(.erros // []) | map(select(.campo == \"$campo\" and (.mensagem | length > 0))) | length > 0"
}

# Teste completo de Bean Validation: status 400 + campo apontado
check_bean_validation() {
    local test_name=$1
    local method=$2
    local path=$3
    local data=$4
    local campo=$5
    request "$method" "$path" "$data"
    if check_status "Bean Validation: $test_name" 400; then
        check_validation_field "Bean Validation: $test_name" "$campo"
    fi
}

# Valida header Location apontando para o recurso criado
check_location() {
    local test_name=$1
    local expected_suffix=$2
    local location
    location=$(echo "$HEADERS" | grep -i '^location:' | awk '{print $2}')
    if [[ "$location" == *"$expected_suffix" ]]; then
        pass "$test_name (Location: $location)"
    else
        fail "$test_name" "Location esperado terminando em '$expected_suffix', recebido: '$location'"
    fi
}

# Valida a estrutura de paginação (compatível com serialização DIRECT e VIA_DTO do Spring)
check_page() {
    local test_name=$1
    local expected_size=$2
    local expected_number=$3
    check_json "$test_name -> possui 'content' (array)" '.content | type == "array"'
    check_json "$test_name -> size = $expected_size" "(.size // .page.size) == $expected_size"
    check_json "$test_name -> page = $expected_number" "(.number // .page.number) == $expected_number"
    check_json "$test_name -> content.length <= size" ".content | length <= $expected_size"
    check_json "$test_name -> possui totalElements e totalPages" \
        '((.totalElements // .page.totalElements) != null) and ((.totalPages // .page.totalPages) != null)'
}

# Valida que o campo está ordenado no content da página
check_sorted() {
    local test_name=$1
    local field=$2
    local direction=$3
    local filter
    if [ "$direction" = "asc" ]; then
        filter="[.content[].$field] as \$v | \$v == (\$v | sort)"
    else
        filter="[.content[].$field] as \$v | \$v == (\$v | sort | reverse)"
    fi
    check_json "$test_name -> ordenado por $field $direction" "$filter"
}

section() {
    echo -e "\n${YELLOW}---> $1${NC}"
}

# ==============================================================================
# 1. CURSOS
# ==============================================================================
section "1. CRUD de CURSOS"

# 1.1 Cadastrar curso ativo
request POST /cursos '{
    "nome": "Engenharia de Software",
    "cargaHoraria": 360,
    "categoria": "Tecnologia",
    "ativo": true
}'
CURSO_ID_1=$(echo "$BODY" | jq -r '.id // empty')
check_status "Cadastrar Curso 1 (Ativo)" 201
check_json "Cadastrar Curso 1 -> retorna id" '.id | type == "number"'
check_json "Cadastrar Curso 1 -> retorna os dados enviados" \
    '.nome == "Engenharia de Software" and .cargaHoraria == 360 and .categoria == "Tecnologia" and .ativo == true'
check_location "Cadastrar Curso 1 -> header Location" "/cursos/$CURSO_ID_1"

# 1.2 Cadastrar curso inativo (usado na regra de negócio)
request POST /cursos '{
    "nome": "Design Gráfico",
    "cargaHoraria": 120,
    "categoria": "Artes",
    "ativo": false
}'
CURSO_ID_INATIVO=$(echo "$BODY" | jq -r '.id // empty')
check_status "Cadastrar Curso 2 (Inativo)" 201
check_json "Cadastrar Curso 2 -> ativo = false" '.ativo == false'

# 1.3 Cadastrar mais um curso (usado na paginação/ordenação)
request POST /cursos '{
    "nome": "Arquitetura de Computadores",
    "cargaHoraria": 80,
    "categoria": "Tecnologia",
    "ativo": true
}'
CURSO_ID_3=$(echo "$BODY" | jq -r '.id // empty')
check_status "Cadastrar Curso 3 (Ativo)" 201

# 1.4 Bean Validation
section "1.4 Bean Validation de CURSOS"
NOME_128=$(printf 'a%.0s' {1..128})

check_bean_validation "Nome < 5 caracteres (@Size)" POST /cursos \
    '{"nome": "Java", "cargaHoraria": 40, "categoria": "TI", "ativo": true}' nome
check_bean_validation "Nome > 127 caracteres (@Size)" POST /cursos \
    "{\"nome\": \"$NOME_128\", \"cargaHoraria\": 40, \"categoria\": \"TI\", \"ativo\": true}" nome
check_bean_validation "Nome em branco (@NotBlank)" POST /cursos \
    '{"nome": "     ", "cargaHoraria": 40, "categoria": "TI", "ativo": true}' nome
check_bean_validation "Nome ausente (@NotBlank)" POST /cursos \
    '{"cargaHoraria": 40, "categoria": "TI", "ativo": true}' nome
check_bean_validation "Carga horária = 0 (@Min)" POST /cursos \
    '{"nome": "Curso Válido", "cargaHoraria": 0, "categoria": "TI", "ativo": true}' cargaHoraria
check_bean_validation "Carga horária negativa (@Min)" POST /cursos \
    '{"nome": "Curso Válido", "cargaHoraria": -10, "categoria": "TI", "ativo": true}' cargaHoraria
check_bean_validation "Carga horária ausente (@NotNull)" POST /cursos \
    '{"nome": "Curso Válido", "categoria": "TI", "ativo": true}' cargaHoraria
check_bean_validation "Categoria em branco (@NotBlank)" POST /cursos \
    '{"nome": "Curso Válido", "cargaHoraria": 40, "categoria": "", "ativo": true}' categoria
check_bean_validation "Ativo ausente (@NotNull)" POST /cursos \
    '{"nome": "Curso Válido", "cargaHoraria": 40, "categoria": "TI"}' ativo

# Várias violações de uma vez retornam todos os campos
request POST /cursos '{}'
check_status "Bean Validation: Corpo vazio" 400
check_json "Bean Validation: Corpo vazio -> retorna erro para todos os campos obrigatórios" \
    '[.erros[].campo] | (index("nome") != null) and (index("cargaHoraria") != null) and (index("categoria") != null) and (index("ativo") != null)'
check_json "Bean Validation: Corpo vazio -> estrutura do erro (status, mensagem, uri, erros)" \
    '.status == 400 and (.mensagem | length > 0) and .uri == "/cursos" and (.erros | type == "array")'

# JSON malformado
request POST /cursos '{"nome": "Curso", '
check_status "JSON malformado é rejeitado" 400

# 1.5 Listar cursos com paginação
section "1.5 Paginação de CURSOS"
request GET "/cursos?page=0&size=5&sort=nome,asc"
check_status "Listar Cursos (page=0&size=5&sort=nome,asc)" 200
check_page "Listar Cursos" 5 0
check_sorted "Listar Cursos" nome asc

request GET "/cursos?page=0&size=5&sort=nome,desc"
check_status "Listar Cursos (sort=nome,desc)" 200
check_sorted "Listar Cursos desc" nome desc

request GET "/cursos?page=0&size=5&sort=cargaHoraria,asc"
check_status "Listar Cursos (sort=cargaHoraria,asc)" 200
check_sorted "Listar Cursos" cargaHoraria asc

request GET "/cursos?page=0&size=1"
check_status "Listar Cursos (size=1)" 200
check_page "Listar Cursos size=1" 1 0
check_json "Listar Cursos size=1 -> retorna exatamente 1 item" '.content | length == 1'
check_json "Listar Cursos size=1 -> totalPages >= 3" '(.totalPages // .page.totalPages) >= 3'

request GET "/cursos?page=1&size=1&sort=id,asc"
check_status "Listar Cursos (page=1&size=1)" 200
check_page "Listar Cursos page=1" 1 1

request GET "/cursos?page=9999&size=5"
check_status "Listar Cursos (página inexistente)" 200
check_json "Listar Cursos página inexistente -> content vazio" '.content | length == 0'

request GET "/cursos"
check_status "Listar Cursos (sem parâmetros, padrão do Pageable)" 200
check_json "Listar Cursos sem parâmetros -> possui content" '.content | type == "array"'

# 1.6 Buscar curso por ID
section "1.6 Busca de CURSOS"
request GET "/cursos/$CURSO_ID_1"
check_status "Buscar Curso por ID ($CURSO_ID_1)" 200
check_json "Buscar Curso por ID -> dados corretos" ".id == $CURSO_ID_1 and .nome == \"Engenharia de Software\""

request GET "/cursos/999999"
check_status "Buscar Curso inexistente" 404
check_json "Buscar Curso inexistente -> corpo de erro (titulo, status, mensagem)" \
    '.status == 404 and (.titulo | length > 0) and (.mensagem | length > 0)'

# 1.7 Atualizar curso
section "1.7 Atualização de CURSOS"
request PUT "/cursos/$CURSO_ID_1" '{
    "nome": "Engenharia de Software Avançada",
    "cargaHoraria": 400,
    "categoria": "Tecnologia",
    "ativo": true
}'
check_status "Atualizar Curso ($CURSO_ID_1)" 200
check_json "Atualizar Curso -> mantém o mesmo id" ".id == $CURSO_ID_1"
check_json "Atualizar Curso -> retorna os dados atualizados" \
    '.nome == "Engenharia de Software Avançada" and .cargaHoraria == 400'

request GET "/cursos/$CURSO_ID_1"
check_json "Atualizar Curso -> alteração persistida (GET)" \
    '.nome == "Engenharia de Software Avançada" and .cargaHoraria == 400'

request PUT "/cursos/999999" '{"nome": "Curso Inexistente", "cargaHoraria": 10, "categoria": "TI", "ativo": true}'
check_status "Atualizar Curso inexistente" 404

check_bean_validation "Atualizar Curso com nome inválido" PUT "/cursos/$CURSO_ID_1" \
    '{"nome": "abc", "cargaHoraria": 10, "categoria": "TI", "ativo": true}' nome
check_bean_validation "Atualizar Curso com carga horária inválida" PUT "/cursos/$CURSO_ID_1" \
    '{"nome": "Curso Válido", "cargaHoraria": 0, "categoria": "TI", "ativo": true}' cargaHoraria

# 1.8 Remover curso inexistente (remoção com sucesso é testada na seção 5)
request DELETE "/cursos/999999"
check_status "Remover Curso inexistente" 404

# ==============================================================================
# 2. ALUNOS
# ==============================================================================
section "2. CRUD de ALUNOS"

# 2.1 Cadastrar aluno maior de 16 anos
request POST /alunos "{
    \"nome\": \"Carlos Eduardo\",
    \"email\": \"carlos.$RUN_ID@gmail.com\",
    \"idade\": 20,
    \"telefone\": \"46999998888\"
}"
ALUNO_ID_1=$(echo "$BODY" | jq -r '.id // empty')
check_status "Cadastrar Aluno 1 (20 anos)" 201
check_json "Cadastrar Aluno 1 -> retorna id e dados" \
    ".id != null and .nome == \"Carlos Eduardo\" and .email == \"carlos.$RUN_ID@gmail.com\" and .idade == 20 and .telefone == \"46999998888\""
check_location "Cadastrar Aluno 1 -> header Location" "/alunos/$ALUNO_ID_1"

# 2.2 Limite: exatamente 16 anos é permitido
request POST /alunos "{
    \"nome\": \"Ana Beatriz\",
    \"email\": \"ana.$RUN_ID@gmail.com\",
    \"idade\": 16
}"
ALUNO_ID_16=$(echo "$BODY" | jq -r '.id // empty')
check_status "Cadastrar Aluno com exatamente 16 anos (limite) e sem telefone (opcional)" 201

# 2.3 Regra de negócio: aluno menor de 16 anos
section "2.3 Regra de Negócio: aluno menor de 16 anos"
request POST /alunos "{
    \"nome\": \"Lucas Gabriel\",
    \"email\": \"lucas.$RUN_ID@gmail.com\",
    \"idade\": 15,
    \"telefone\": \"46988887777\"
}"
if check_status "Impedir cadastro de Aluno com 15 anos" 400 422; then
    check_validation_field "Impedir cadastro de Aluno com 15 anos" idade
fi

request PUT "/alunos/$ALUNO_ID_1" "{
    \"nome\": \"Carlos Eduardo\",
    \"email\": \"carlos.$RUN_ID@gmail.com\",
    \"idade\": 15
}"
check_status "Impedir atualizar Aluno para idade < 16" 400 422

request GET "/alunos/$ALUNO_ID_1"
check_json "Aluno não foi alterado após atualização inválida" '.idade == 20'

# 2.4 Bean Validation
section "2.4 Bean Validation de ALUNOS"
check_bean_validation "E-mail inválido (@Email)" POST /alunos \
    '{"nome": "Mariana Silva", "email": "email-invalido", "idade": 18}' email
check_bean_validation "E-mail em branco (@NotBlank)" POST /alunos \
    '{"nome": "Mariana Silva", "email": "", "idade": 18}' email
check_bean_validation "E-mail ausente (@NotBlank)" POST /alunos \
    '{"nome": "Mariana Silva", "idade": 18}' email
check_bean_validation "Nome em branco (@NotBlank)" POST /alunos \
    '{"nome": "   ", "email": "mariana@gmail.com", "idade": 18}' nome
check_bean_validation "Nome com 1 caractere (@Size)" POST /alunos \
    '{"nome": "M", "email": "mariana@gmail.com", "idade": 18}' nome
check_bean_validation "Idade ausente (@NotNull)" POST /alunos \
    '{"nome": "Mariana Silva", "email": "mariana@gmail.com"}' idade
check_bean_validation "Idade negativa (@Positive / @Min)" POST /alunos \
    '{"nome": "Mariana Silva", "email": "mariana@gmail.com", "idade": -5}' idade
check_bean_validation "Idade zero (@Positive / @Min)" POST /alunos \
    '{"nome": "Mariana Silva", "email": "mariana@gmail.com", "idade": 0}' idade

request POST /alunos '{}'
check_status "Bean Validation: Corpo vazio" 400
check_json "Bean Validation: Corpo vazio -> retorna erro para nome, email e idade" \
    '[.erros[].campo] | (index("nome") != null) and (index("email") != null) and (index("idade") != null)'

# 2.5 Listar alunos com paginação
section "2.5 Paginação de ALUNOS"
request GET "/alunos?page=0&size=5&sort=nome,asc"
check_status "Listar Alunos (page=0&size=5&sort=nome,asc)" 200
check_page "Listar Alunos" 5 0
check_sorted "Listar Alunos" nome asc

request GET "/alunos?page=0&size=5&sort=idade,desc"
check_status "Listar Alunos (sort=idade,desc)" 200
check_sorted "Listar Alunos" idade desc

request GET "/alunos?page=0&size=1"
check_status "Listar Alunos (size=1)" 200
check_json "Listar Alunos size=1 -> retorna exatamente 1 item" '.content | length == 1'

# 2.6 Buscar aluno por ID
section "2.6 Busca de ALUNOS"
request GET "/alunos/$ALUNO_ID_1"
check_status "Buscar Aluno por ID ($ALUNO_ID_1)" 200
check_json "Buscar Aluno por ID -> dados corretos" ".id == $ALUNO_ID_1 and .idade == 20"

request GET "/alunos/999999"
check_status "Buscar Aluno inexistente" 404
check_json "Buscar Aluno inexistente -> corpo de erro (titulo, status, mensagem)" \
    '.status == 404 and (.titulo | length > 0) and (.mensagem | length > 0)'

# 2.7 Atualizar aluno
section "2.7 Atualização de ALUNOS"
request PUT "/alunos/$ALUNO_ID_1" "{
    \"nome\": \"Carlos Eduardo Silva\",
    \"email\": \"carlos.silva.$RUN_ID@gmail.com\",
    \"idade\": 21,
    \"telefone\": \"46999998888\"
}"
check_status "Atualizar Aluno ($ALUNO_ID_1)" 200
check_json "Atualizar Aluno -> mantém o mesmo id" ".id == $ALUNO_ID_1"
check_json "Atualizar Aluno -> retorna os dados atualizados" \
    ".nome == \"Carlos Eduardo Silva\" and .email == \"carlos.silva.$RUN_ID@gmail.com\" and .idade == 21"

request GET "/alunos/$ALUNO_ID_1"
check_json "Atualizar Aluno -> alteração persistida (GET)" '.nome == "Carlos Eduardo Silva" and .idade == 21'

request PUT "/alunos/999999" '{"nome": "Fulano", "email": "fulano@gmail.com", "idade": 20}'
check_status "Atualizar Aluno inexistente" 404

check_bean_validation "Atualizar Aluno com e-mail inválido" PUT "/alunos/$ALUNO_ID_1" \
    '{"nome": "Carlos", "email": "invalido", "idade": 21}' email

# 2.8 Remover aluno inexistente (remoção com sucesso é testada na seção 5)
request DELETE "/alunos/999999"
check_status "Remover Aluno inexistente" 404

# ==============================================================================
# 3. MATRÍCULAS
# ==============================================================================
section "3. CRUD de MATRÍCULAS"

# 3.1 Realizar matrícula válida
request POST /matriculas "{
    \"dataMatricula\": \"$HOJE\",
    \"status\": \"ATIVA\",
    \"aluno\": $ALUNO_ID_1,
    \"curso\": $CURSO_ID_1
}"
MATRICULA_ID_1=$(echo "$BODY" | jq -r '.id // empty')
check_status "Realizar Matrícula válida (data de hoje)" 201
check_json "Realizar Matrícula -> retorna id, aluno, curso, status e data" \
    ".id != null and .aluno == $ALUNO_ID_1 and .curso == $CURSO_ID_1 and .status == \"ATIVA\" and .dataMatricula == \"$HOJE\""
check_location "Realizar Matrícula -> header Location" "/matriculas/$MATRICULA_ID_1"

# 3.2 Matrícula com data futura (permitido)
request POST /matriculas "{
    \"dataMatricula\": \"$AMANHA\",
    \"status\": \"ATIVA\",
    \"aluno\": $ALUNO_ID_16,
    \"curso\": $CURSO_ID_3
}"
MATRICULA_ID_2=$(echo "$BODY" | jq -r '.id // empty')
check_status "Realizar Matrícula com data futura" 201

# 3.3 Regras de negócio na matrícula
section "3.3 Regras de Negócio na MATRÍCULA"
request POST /matriculas "{
    \"dataMatricula\": \"$HOJE\",
    \"status\": \"ATIVA\",
    \"aluno\": $ALUNO_ID_1,
    \"curso\": $CURSO_ID_INATIVO
}"
check_status "Impedir Matrícula em Curso Inativo" 400 409 422
check_json "Impedir Matrícula em Curso Inativo -> corpo de erro com mensagem" '(.mensagem | length > 0)'

# Curso que fica inativo depois: novas matrículas também devem ser bloqueadas
request PUT "/cursos/$CURSO_ID_3" '{"nome": "Arquitetura de Computadores", "cargaHoraria": 80, "categoria": "Tecnologia", "ativo": false}'
check_status "Inativar Curso 3" 200
request POST /matriculas "{
    \"dataMatricula\": \"$HOJE\",
    \"status\": \"ATIVA\",
    \"aluno\": $ALUNO_ID_1,
    \"curso\": $CURSO_ID_3
}"
check_status "Impedir Matrícula em curso que foi inativado" 400 409 422

request PUT "/matriculas/$MATRICULA_ID_1" "{
    \"dataMatricula\": \"$HOJE\",
    \"status\": \"ATIVA\",
    \"aluno\": $ALUNO_ID_1,
    \"curso\": $CURSO_ID_INATIVO
}"
check_status "Impedir transferir Matrícula para Curso Inativo (PUT)" 400 409 422

# Aluno menor de 16: a API impede o cadastro/atualização (seção 2.3),
# então nunca existe um aluno < 16 para ser matriculado. Validamos aqui que a
# tentativa de cadastrar e matricular um menor falha em toda a cadeia.
request POST /alunos "{\"nome\": \"Pedro Henrique\", \"email\": \"pedro.$RUN_ID@gmail.com\", \"idade\": 12}"
ALUNO_ID_MENOR=$(echo "$BODY" | jq -r '.id // empty')
check_status "Impedir Matrícula de menor de 16: cadastro do aluno com 12 anos é bloqueado" 400 422
if [ -n "$ALUNO_ID_MENOR" ]; then
    request POST /matriculas "{\"dataMatricula\": \"$HOJE\", \"status\": \"ATIVA\", \"aluno\": $ALUNO_ID_MENOR, \"curso\": $CURSO_ID_1}"
    check_status "Impedir Matrícula de aluno menor de 16 anos" 400 422
else
    pass "Impedir Matrícula de menor de 16: nenhum aluno menor foi criado, matrícula impossível"
fi

request POST /matriculas "{
    \"dataMatricula\": \"$HOJE\",
    \"status\": \"ATIVA\",
    \"aluno\": 999999,
    \"curso\": $CURSO_ID_1
}"
check_status "Matrícula com aluno inexistente" 404

request POST /matriculas "{
    \"dataMatricula\": \"$HOJE\",
    \"status\": \"ATIVA\",
    \"aluno\": $ALUNO_ID_1,
    \"curso\": 999999
}"
check_status "Matrícula com curso inexistente" 404

# 3.4 Bean Validation
section "3.4 Bean Validation de MATRÍCULAS"
check_bean_validation "Data no passado (@FutureOrPresent)" POST /matriculas \
    "{\"dataMatricula\": \"$ONTEM\", \"status\": \"ATIVA\", \"aluno\": $ALUNO_ID_1, \"curso\": $CURSO_ID_1}" dataMatricula
check_bean_validation "Data ausente (@NotNull)" POST /matriculas \
    "{\"status\": \"ATIVA\", \"aluno\": $ALUNO_ID_1, \"curso\": $CURSO_ID_1}" dataMatricula
check_bean_validation "Status em branco (@NotBlank)" POST /matriculas \
    "{\"dataMatricula\": \"$HOJE\", \"status\": \"\", \"aluno\": $ALUNO_ID_1, \"curso\": $CURSO_ID_1}" status
check_bean_validation "Aluno ausente (@NotNull)" POST /matriculas \
    "{\"dataMatricula\": \"$HOJE\", \"status\": \"ATIVA\", \"curso\": $CURSO_ID_1}" aluno
check_bean_validation "Aluno com id negativo (@Positive)" POST /matriculas \
    "{\"dataMatricula\": \"$HOJE\", \"status\": \"ATIVA\", \"aluno\": -1, \"curso\": $CURSO_ID_1}" aluno
check_bean_validation "Curso ausente (@NotNull)" POST /matriculas \
    "{\"dataMatricula\": \"$HOJE\", \"status\": \"ATIVA\", \"aluno\": $ALUNO_ID_1}" curso
check_bean_validation "Curso com id zero (@Positive)" POST /matriculas \
    "{\"dataMatricula\": \"$HOJE\", \"status\": \"ATIVA\", \"aluno\": $ALUNO_ID_1, \"curso\": 0}" curso

request POST /matriculas '{}'
check_status "Bean Validation: Corpo vazio" 400
check_json "Bean Validation: Corpo vazio -> retorna erro para dataMatricula, status, aluno e curso" \
    '[.erros[].campo] | (index("dataMatricula") != null) and (index("status") != null) and (index("aluno") != null) and (index("curso") != null)'

# 3.5 Listar matrículas com paginação
section "3.5 Paginação de MATRÍCULAS"
request GET "/matriculas?page=0&size=5&sort=dataMatricula,desc"
check_status "Listar Matrículas (page=0&size=5&sort=dataMatricula,desc)" 200
check_page "Listar Matrículas" 5 0
check_sorted "Listar Matrículas" dataMatricula desc

request GET "/matriculas?page=0&size=1&sort=id,asc"
check_status "Listar Matrículas (size=1)" 200
check_json "Listar Matrículas size=1 -> retorna exatamente 1 item" '.content | length == 1'
check_json "Listar Matrículas size=1 -> totalElements >= 2" '(.totalElements // .page.totalElements) >= 2'

# 3.6 Buscar matrícula por ID
section "3.6 Busca de MATRÍCULAS"
request GET "/matriculas/$MATRICULA_ID_1"
check_status "Buscar Matrícula por ID ($MATRICULA_ID_1)" 200
check_json "Buscar Matrícula por ID -> dados corretos" \
    ".id == $MATRICULA_ID_1 and .aluno == $ALUNO_ID_1 and .curso == $CURSO_ID_1"

request GET "/matriculas/999999"
check_status "Buscar Matrícula inexistente" 404
check_json "Buscar Matrícula inexistente -> corpo de erro (titulo, status, mensagem)" \
    '.status == 404 and (.titulo | length > 0) and (.mensagem | length > 0)'

# 3.7 Atualizar status da matrícula
section "3.7 Atualização de MATRÍCULAS"
request PUT "/matriculas/$MATRICULA_ID_1" "{
    \"dataMatricula\": \"$HOJE\",
    \"status\": \"CONCLUIDA\",
    \"aluno\": $ALUNO_ID_1,
    \"curso\": $CURSO_ID_1
}"
check_status "Atualizar Status da Matrícula" 200
check_json "Atualizar Status da Matrícula -> mantém o mesmo id e retorna novo status" \
    ".id == $MATRICULA_ID_1 and .status == \"CONCLUIDA\""

request GET "/matriculas/$MATRICULA_ID_1"
check_json "Atualizar Status da Matrícula -> alteração persistida (GET)" '.status == "CONCLUIDA"'

request PUT "/matriculas/999999" "{
    \"dataMatricula\": \"$HOJE\",
    \"status\": \"ATIVA\",
    \"aluno\": $ALUNO_ID_1,
    \"curso\": $CURSO_ID_1
}"
check_status "Atualizar Matrícula inexistente" 404

check_bean_validation "Atualizar Matrícula com status em branco" PUT "/matriculas/$MATRICULA_ID_1" \
    "{\"dataMatricula\": \"$HOJE\", \"status\": \" \", \"aluno\": $ALUNO_ID_1, \"curso\": $CURSO_ID_1}" status

# ==============================================================================
# 4. REGRAS DE EXCLUSÃO COM MATRÍCULAS
# ==============================================================================
section "4. Regras de Negócio: exclusão com matrículas"

request DELETE "/cursos/$CURSO_ID_1"
check_status "Impedir Exclusão de Curso com Matrículas" 400 409 422
check_json "Impedir Exclusão de Curso -> corpo de erro com mensagem" '(.mensagem | length > 0)'
request GET "/cursos/$CURSO_ID_1"
check_status "Curso com matrículas continua existindo após tentativa de exclusão" 200

request DELETE "/alunos/$ALUNO_ID_1"
check_status "Impedir Exclusão de Aluno com Matrículas" 400 409 422
check_json "Impedir Exclusão de Aluno -> corpo de erro com mensagem" '(.mensagem | length > 0)'
request GET "/alunos/$ALUNO_ID_1"
check_status "Aluno com matrículas continua existindo após tentativa de exclusão" 200

# ==============================================================================
# 5. CANCELAMENTO DE MATRÍCULA E REMOÇÕES
# ==============================================================================
section "5. Cancelamento de matrícula e remoções"

# 5.1 Cancelar matrícula
request DELETE "/matriculas/$MATRICULA_ID_1"
check_status "Cancelar Matrícula ($MATRICULA_ID_1)" 200 204
request GET "/matriculas/$MATRICULA_ID_1"
check_status "Matrícula cancelada não é mais encontrada" 404
request DELETE "/matriculas/$MATRICULA_ID_1"
check_status "Cancelar Matrícula já cancelada / inexistente" 404

# 5.2 Remover curso sem matrículas
request DELETE "/cursos/$CURSO_ID_1"
check_status "Remover Curso sem Matrículas ($CURSO_ID_1)" 200 204
request GET "/cursos/$CURSO_ID_1"
check_status "Curso removido não é mais encontrado" 404

# 5.3 Remover aluno sem matrículas
request DELETE "/alunos/$ALUNO_ID_1"
check_status "Remover Aluno sem Matrículas ($ALUNO_ID_1)" 200 204
request GET "/alunos/$ALUNO_ID_1"
check_status "Aluno removido não é mais encontrado" 404

# 5.4 Limpeza dos demais dados criados
request DELETE "/matriculas/$MATRICULA_ID_2"
check_status "Cancelar Matrícula 2 ($MATRICULA_ID_2)" 200 204
request DELETE "/alunos/$ALUNO_ID_16"
check_status "Remover Aluno 2 ($ALUNO_ID_16)" 200 204
request DELETE "/cursos/$CURSO_ID_3"
check_status "Remover Curso 3 ($CURSO_ID_3)" 200 204
request DELETE "/cursos/$CURSO_ID_INATIVO"
check_status "Remover Curso Inativo ($CURSO_ID_INATIVO)" 200 204

# ==============================================================================
# RESUMO
# ==============================================================================
TOTAL=$((PASS + FAIL))
echo -e "\n${BLUE}======================================================================${NC}"
echo -e "${BLUE}                    Bateria de Testes Concluída!                      ${NC}"
echo -e "${BLUE}======================================================================${NC}"
echo -e "Total: $TOTAL | ${GREEN}Passaram: $PASS${NC} | ${RED}Falharam: $FAIL${NC}"

if [ "$FAIL" -gt 0 ]; then
    echo -e "\n${RED}Testes que falharam:${NC}"
    for t in "${FAILED_TESTS[@]}"; do
        echo -e "  ${RED}- $t${NC}"
    done
    exit 1
fi

exit 0
