#!/usr/bin/env bash
# =============================================================================
#  Testes automatizados da API REST - Gerenciamento de Cursos e Matrículas
# =============================================================================
#  Uso:
#    ./test_api.sh                         # usa http://localhost:8080
#    ./test_api.sh http://localhost:8081   # outra URL base
#    VERBOSE=1 ./test_api.sh               # mostra a resposta de cada requisição
#
#  Resultados:
#    OK    -> comportamento esperado
#    FAIL  -> quebra um requisito da especificação
#    WARN  -> funciona, mas poderia ser melhor (ex.: mensagem de erro não cita o campo)
#
#  Código de saída: 0 se nenhum teste falhou, 1 caso contrário.
# =============================================================================

BASE_URL="${1:-http://localhost:8080}"
VERBOSE="${VERBOSE:-0}"
STATUS_INICIAL="${STATUS_INICIAL:-ATIVO}"
STATUS_FINAL="${STATUS_FINAL:-CONCLUIDO}"
DATA_MATRICULA="$(date +%F)"
RUN_ID="$(date +%s)"          # deixa e-mails únicos entre execuções
ID_INEXISTENTE=999999999

GREEN='\033[0;32m'; RED='\033[0;31m'; BLUE='\033[0;34m'
YELLOW='\033[1;33m'; GRAY='\033[0;90m'; BOLD='\033[1m'; NC='\033[0m'

PASS=0; FAIL=0; WARN=0
FAILED_TESTS=()

# -----------------------------------------------------------------------------
# Dependências
# -----------------------------------------------------------------------------
for cmd in curl jq; do
    if ! command -v "$cmd" &> /dev/null; then
        echo -e "${RED}Erro: '$cmd' não está instalado (ex.: sudo apt install $cmd).${NC}"
        exit 1
    fi
done

BODY_FILE="$(mktemp)"; HEADER_FILE="$(mktemp)"
trap 'rm -f "$BODY_FILE" "$HEADER_FILE"' EXIT

# -----------------------------------------------------------------------------
# Funções auxiliares
# -----------------------------------------------------------------------------

# Encurta um texto para exibir no terminal
short() {
    local s
    s="$(printf '%s' "$1" | tr -d '\n')"
    [ ${#s} -gt 300 ] && s="${s:0:300}..."
    printf '%s' "$s"
}

# request MÉTODO CAMINHO [CORPO]  -> preenche HTTP_CODE e BODY
request() {
    local method=$1 path=$2
    local args=(-s -o "$BODY_FILE" -D "$HEADER_FILE" -w '%{http_code}' -X "$method" "$BASE_URL$path")
    if [ $# -ge 3 ]; then
        args+=(-H 'Content-Type: application/json' --data-raw "$3")
    fi
    HTTP_CODE="$(curl "${args[@]}")"
    BODY="$(cat "$BODY_FILE")"
    if [ "$VERBOSE" = "1" ]; then
        echo -e "         ${GRAY}$method $path -> $HTTP_CODE  $(short "$BODY")${NC}"
    fi
}

pass() { PASS=$((PASS + 1)); echo -e "  [ ${GREEN}OK${NC}   ] $1"; }
warn() {
    WARN=$((WARN + 1)); echo -e "  [ ${YELLOW}WARN${NC} ] $1"
    [ -n "${2-}" ] && echo -e "           ${GRAY}$2${NC}"
}
fail() {
    FAIL=$((FAIL + 1)); FAILED_TESTS+=("$1"); echo -e "  [ ${RED}FAIL${NC} ] $1"
    [ -n "${2-}" ] && echo -e "           ${GRAY}$2${NC}"
}

section() { echo -e "\n${YELLOW}${BOLD}---> $1${NC}"; }

# expect_status DESCRIÇÃO CÓDIGO [CÓDIGO...]
expect_status() {
    local desc=$1; shift
    local code expected
    for code in "$@"; do
        if [ "$HTTP_CODE" = "$code" ]; then
            pass "$desc (HTTP $HTTP_CODE)"
            return 0
        fi
    done
    expected="$(IFS=/; echo "$*")"
    fail "$desc (esperado $expected, recebido $HTTP_CODE)" "Resposta: $(short "$BODY")"
    return 1
}

# expect_json DESCRIÇÃO FILTRO_JQ [args do jq...]  -> o filtro deve resultar em true
expect_json() {
    local desc=$1 filter=$2; shift 2
    if printf '%s' "$BODY" | jq -e "$@" "$filter" > /dev/null 2>&1; then
        pass "$desc"
    else
        fail "$desc" "Resposta: $(short "$BODY")"
    fi
}

# expect_location DESCRIÇÃO  -> POST deve devolver header Location
expect_location() {
    if grep -qi '^location:' "$HEADER_FILE"; then
        pass "$1"
    else
        warn "$1: header Location ausente"
    fi
}

json_id() { printf '%s' "$BODY" | jq -r '.id // empty' 2> /dev/null; }

# mut JSON FILTRO_JQ  -> devolve o JSON modificado (para montar casos inválidos)
mut() { printf '%s' "$1" | jq -c "$2"; }

# invalid DESCRIÇÃO MÉTODO CAMINHO CORPO [CAMPO]
#   Espera HTTP 400. Se CAMPO for informado, verifica se a mensagem de erro o cita.
#   Se a API aceitar por engano (201), remove o registro criado.
invalid() {
    local desc=$1 method=$2 path=$3 data=$4 field=${5-}
    request "$method" "$path" "$data"
    if expect_status "$desc" 400; then
        if [ -n "$field" ] && ! printf '%s' "$BODY" | grep -qi -- "$field"; then
            warn "   ↳ mensagem de erro não cita o campo '$field'" "Resposta: $(short "$BODY")"
        fi
    elif [ "$HTTP_CODE" = "201" ] && [ "$method" = "POST" ]; then
        local id; id="$(json_id)"
        [ -n "$id" ] && curl -s -o /dev/null -X DELETE "$BASE_URL$path/$id"
    fi
}

# Monta o JSON de uma matrícula: matricula_json ALUNO CURSO [STATUS] [DATA]
matricula_json() {
    jq -nc --argjson a "$1" --argjson c "$2" \
        --arg s "${3:-$STATUS_INICIAL}" --arg d "${4:-$DATA_MATRICULA}" \
        '{dataMatricula: $d, status: $s, aluno: $a, curso: $c}'
}

aluno_json() { # aluno_json NOME EMAIL IDADE [TELEFONE]
    if [ -n "${4-}" ]; then
        jq -nc --arg n "$1" --arg e "$2" --argjson i "$3" --arg t "$4" \
            '{nome: $n, email: $e, idade: $i, telefone: $t}'
    else
        jq -nc --arg n "$1" --arg e "$2" --argjson i "$3" '{nome: $n, email: $e, idade: $i}'
    fi
}

# Aceita respostas em que aluno/curso vêm como id, como objeto ou como alunoId/cursoId
REF_FILTER='def ref(k): (.[k + "Id"] // (if (.[k] | type) == "object" then .[k].id else .[k] end));'

# Aceita Page serializado direto ou no formato { content, page: {...} }
PAGE_FILTER='def psize: (.size // .page.size); def pnum: (.number // .page.number); def ptotal: (.totalElements // .page.totalElements);'

summary() {
    echo -e "\n${BLUE}======================================================================${NC}"
    echo -e "  ${GREEN}OK: $PASS${NC}    ${RED}FAIL: $FAIL${NC}    ${YELLOW}WARN: $WARN${NC}"
    if [ $FAIL -gt 0 ]; then
        echo -e "\n  ${RED}${BOLD}Testes que falharam:${NC}"
        for t in "${FAILED_TESTS[@]}"; do echo -e "   ${RED}•${NC} $t"; done
    fi
    echo -e "${BLUE}======================================================================${NC}"
}

# require VALOR DESCRIÇÃO  -> aborta se um dado essencial não foi criado
require() {
    if [ -z "$1" ]; then
        echo -e "\n${RED}${BOLD}Não foi possível criar $2. Os testes seguintes dependem disso; abortando.${NC}"
        summary
        exit 1
    fi
}

echo -e "${BLUE}======================================================================${NC}"
echo -e "${BLUE}${BOLD}   Testes da API REST - Cursos e Matrículas  ($BASE_URL)${NC}"
echo -e "${BLUE}======================================================================${NC}"

# -----------------------------------------------------------------------------
# 0. Conectividade
# -----------------------------------------------------------------------------
request GET "/cursos"
if [ "$HTTP_CODE" = "000" ]; then
    echo -e "\n${RED}Não foi possível conectar em $BASE_URL. A aplicação está rodando?${NC}"
    exit 1
fi

# =============================================================================
# 1. CURSOS - CRUD
# =============================================================================
section "1. CURSOS - CRUD"

CURSO_BASE='{"nome":"Engenharia de Software","cargaHoraria":360,"categoria":"Tecnologia","ativo":true}'

request POST "/cursos" "$CURSO_BASE"
expect_status "Cadastrar curso ativo" 201
CURSO_ID="$(json_id)"
expect_location "   ↳ POST /cursos devolve header Location"
expect_json "   ↳ resposta contém os dados enviados" \
    '.id != null and .nome == "Engenharia de Software" and .cargaHoraria == 360 and .ativo == true'
require "$CURSO_ID" "o curso ativo"

request POST "/cursos" '{"nome":"Design Gráfico","cargaHoraria":120,"categoria":"Artes","ativo":false}'
expect_status "Cadastrar curso inativo" 201
CURSO_INATIVO_ID="$(json_id)"
require "$CURSO_INATIVO_ID" "o curso inativo"

request POST "/cursos" '{"nome":"Redes","cargaHoraria":1,"categoria":"TI","ativo":true}'
expect_status "Limite: nome com exatamente 5 caracteres e cargaHoraria = 1 são aceitos" 201
ID_TMP="$(json_id)"
[ -n "$ID_TMP" ] && curl -s -o /dev/null -X DELETE "$BASE_URL/cursos/$ID_TMP"

request GET "/cursos/$CURSO_ID"
expect_status "Buscar curso por ID" 200
expect_json "   ↳ retorna o curso correto" '.id == $id and .nome == "Engenharia de Software"' --argjson id "$CURSO_ID"

request GET "/cursos/$ID_INEXISTENTE"
expect_status "Buscar curso inexistente" 404

request PUT "/cursos/$CURSO_ID" \
    '{"nome":"Engenharia de Software Avançada","cargaHoraria":400,"categoria":"Tecnologia","ativo":true}'
expect_status "Atualizar curso" 200
expect_json "   ↳ resposta reflete a atualização" \
    '.id == $id and .nome == "Engenharia de Software Avançada" and .cargaHoraria == 400' --argjson id "$CURSO_ID"

request GET "/cursos/$CURSO_ID"
expect_json "   ↳ atualização foi persistida (GET após PUT)" '.cargaHoraria == 400'

request PUT "/cursos/$ID_INEXISTENTE" "$CURSO_BASE"
expect_status "Atualizar curso inexistente" 404

# =============================================================================
# 2. CURSOS - Bean Validation
# =============================================================================
section "2. CURSOS - Bean Validation (espera HTTP 400)"

invalid "Corpo vazio {}"                         POST "/cursos" '{}'                                        nome
invalid "nome ausente"                           POST "/cursos" "$(mut "$CURSO_BASE" 'del(.nome)')"          nome
invalid "nome só com espaços"                    POST "/cursos" "$(mut "$CURSO_BASE" '.nome = "      "')"     nome
invalid "nome com 4 caracteres"                  POST "/cursos" "$(mut "$CURSO_BASE" '.nome = "Java"')"       nome
invalid "cargaHoraria ausente"                   POST "/cursos" "$(mut "$CURSO_BASE" 'del(.cargaHoraria)')"  cargaHoraria
invalid "cargaHoraria = 0"                       POST "/cursos" "$(mut "$CURSO_BASE" '.cargaHoraria = 0')"   cargaHoraria
invalid "cargaHoraria negativa"                  POST "/cursos" "$(mut "$CURSO_BASE" '.cargaHoraria = -10')" cargaHoraria
invalid "categoria ausente"                      POST "/cursos" "$(mut "$CURSO_BASE" 'del(.categoria)')"     categoria
invalid "categoria em branco"                    POST "/cursos" "$(mut "$CURSO_BASE" '.categoria = ""')"     categoria
invalid "ativo ausente"                          POST "/cursos" "$(mut "$CURSO_BASE" 'del(.ativo)')"         ativo
invalid "cargaHoraria com tipo errado (\"abc\")" POST "/cursos" "$(mut "$CURSO_BASE" '.cargaHoraria = "abc"')"
invalid "JSON malformado"                        POST "/cursos" '{"nome": "Curso sem fechar'
invalid "PUT com nome curto também é validado"   PUT  "/cursos/$CURSO_ID" "$(mut "$CURSO_BASE" '.nome = "ABC"')" nome
invalid "PUT com cargaHoraria = 0"               PUT  "/cursos/$CURSO_ID" "$(mut "$CURSO_BASE" '.cargaHoraria = 0')" cargaHoraria

# =============================================================================
# 3. ALUNOS - CRUD
# =============================================================================
section "3. ALUNOS - CRUD"

ALUNO_BASE="$(aluno_json "Carlos Eduardo" "carlos.$RUN_ID@teste.com" 20 "46999998888")"

request POST "/alunos" "$ALUNO_BASE"
expect_status "Cadastrar aluno (20 anos)" 201
ALUNO_ID="$(json_id)"
expect_location "   ↳ POST /alunos devolve header Location"
expect_json "   ↳ resposta contém os dados enviados" \
    '.id != null and .nome == "Carlos Eduardo" and .email == $e and .idade == 20' --arg e "carlos.$RUN_ID@teste.com"
require "$ALUNO_ID" "o aluno base"

request POST "/alunos" "$(aluno_json "Ana Paula" "ana.$RUN_ID@teste.com" 16)"
expect_status "Limite: aluno com exatamente 16 anos e sem telefone (opcional) é aceito" 201
ID_TMP="$(json_id)"
[ -n "$ID_TMP" ] && curl -s -o /dev/null -X DELETE "$BASE_URL/alunos/$ID_TMP"

request GET "/alunos/$ALUNO_ID"
expect_status "Buscar aluno por ID" 200
expect_json "   ↳ retorna o aluno correto" '.id == $id and .nome == "Carlos Eduardo"' --argjson id "$ALUNO_ID"

request GET "/alunos/$ID_INEXISTENTE"
expect_status "Buscar aluno inexistente" 404

request PUT "/alunos/$ALUNO_ID" "$(aluno_json "Carlos Eduardo Silva" "carlos.silva.$RUN_ID@teste.com" 21 "46999998888")"
expect_status "Atualizar aluno" 200
expect_json "   ↳ resposta reflete a atualização" '.nome == "Carlos Eduardo Silva" and .idade == 21'

request GET "/alunos/$ALUNO_ID"
expect_json "   ↳ atualização foi persistida (GET após PUT)" '.idade == 21'

request PUT "/alunos/$ID_INEXISTENTE" "$ALUNO_BASE"
expect_status "Atualizar aluno inexistente" 404

# =============================================================================
# 4. ALUNOS - Bean Validation
# =============================================================================
section "4. ALUNOS - Bean Validation (espera HTTP 400)"

invalid "Corpo vazio {}"                      POST "/alunos" '{}'                                           nome
invalid "nome ausente"                        POST "/alunos" "$(mut "$ALUNO_BASE" 'del(.nome)')"             nome
invalid "nome em branco"                      POST "/alunos" "$(mut "$ALUNO_BASE" '.nome = "   "')"          nome
invalid "email ausente"                       POST "/alunos" "$(mut "$ALUNO_BASE" 'del(.email)')"            email
invalid "email inválido (sem @)"              POST "/alunos" "$(mut "$ALUNO_BASE" '.email = "email-invalido"')" email
invalid "email inválido (sem domínio)"        POST "/alunos" "$(mut "$ALUNO_BASE" '.email = "carlos@"')"     email
invalid "idade 15 (menor de 16)"              POST "/alunos" "$(mut "$ALUNO_BASE" '.idade = 15')"            idade
invalid "idade 0"                             POST "/alunos" "$(mut "$ALUNO_BASE" '.idade = 0')"             idade
invalid "idade com tipo errado (\"vinte\")"   POST "/alunos" "$(mut "$ALUNO_BASE" '.idade = "vinte"')"
invalid "PUT com idade 15 também é validado"  PUT  "/alunos/$ALUNO_ID" "$(mut "$ALUNO_BASE" '.idade = 15')"  idade
invalid "PUT com email inválido"              PUT  "/alunos/$ALUNO_ID" "$(mut "$ALUNO_BASE" '.email = "x"')" email

# idade ausente: a especificação só diz "mínimo 16", mas sem @NotNull um aluno
# sem idade burla a regra "impedir matrícula de aluno menor de 16 anos".
request POST "/alunos" "$(mut "$ALUNO_BASE" 'del(.idade) | .email = "sem.idade.'"$RUN_ID"'@teste.com"')"
if [ "$HTTP_CODE" = "400" ]; then
    pass "idade ausente é rejeitada (HTTP 400)"
else
    warn "idade ausente foi aceita (HTTP $HTTP_CODE): considere @NotNull em idade"
    ID_TMP="$(json_id)"
    [ -n "$ID_TMP" ] && curl -s -o /dev/null -X DELETE "$BASE_URL/alunos/$ID_TMP"
fi

# =============================================================================
# 5. MATRÍCULAS - CRUD e regras de negócio
# =============================================================================
section "5. MATRÍCULAS - CRUD e regras de negócio"

request POST "/matriculas" "$(matricula_json "$ALUNO_ID" "$CURSO_INATIVO_ID")"
expect_status "Regra: impedir matrícula em curso inativo" 400 409 422

# Curso que estava ativo e foi desativado via PUT
request POST "/cursos" '{"nome":"Curso Desativado Depois","cargaHoraria":30,"categoria":"Teste","ativo":true}'
CURSO_TMP_ID="$(json_id)"
if [ -n "$CURSO_TMP_ID" ]; then
    request PUT "/cursos/$CURSO_TMP_ID" '{"nome":"Curso Desativado Depois","cargaHoraria":30,"categoria":"Teste","ativo":false}'
    request POST "/matriculas" "$(matricula_json "$ALUNO_ID" "$CURSO_TMP_ID")"
    expect_status "Regra: impedir matrícula em curso desativado via PUT" 400 409 422
    if [ "$HTTP_CODE" = "201" ]; then
        ID_TMP="$(json_id)"; [ -n "$ID_TMP" ] && curl -s -o /dev/null -X DELETE "$BASE_URL/matriculas/$ID_TMP"
    fi
    curl -s -o /dev/null -X DELETE "$BASE_URL/cursos/$CURSO_TMP_ID"
fi

request POST "/matriculas" "$(matricula_json "$ALUNO_ID" "$CURSO_ID")"
expect_status "Realizar matrícula válida" 201
MATRICULA_ID="$(json_id)"
expect_location "   ↳ POST /matriculas devolve header Location"
expect_json "   ↳ resposta contém data, status, aluno e curso corretos" \
    "$REF_FILTER"' .id != null and .dataMatricula == $d and .status == $s and ref("aluno") == $a and ref("curso") == $c' \
    --arg d "$DATA_MATRICULA" --arg s "$STATUS_INICIAL" --argjson a "$ALUNO_ID" --argjson c "$CURSO_ID"
require "$MATRICULA_ID" "a matrícula válida"

request GET "/matriculas/$MATRICULA_ID"
expect_status "Buscar matrícula por ID" 200
expect_json "   ↳ retorna a matrícula correta" '.id == $id' --argjson id "$MATRICULA_ID"

request GET "/matriculas/$ID_INEXISTENTE"
expect_status "Buscar matrícula inexistente" 404

request POST "/matriculas" "$(matricula_json "$ID_INEXISTENTE" "$CURSO_ID")"
expect_status "Matrícula com aluno inexistente" 404 400 422

request POST "/matriculas" "$(matricula_json "$ALUNO_ID" "$ID_INEXISTENTE")"
expect_status "Matrícula com curso inexistente" 404 400 422

request PUT "/matriculas/$MATRICULA_ID" "$(matricula_json "$ALUNO_ID" "$CURSO_ID" "$STATUS_FINAL")"
expect_status "Atualizar status da matrícula para $STATUS_FINAL" 200
expect_json "   ↳ resposta tem o novo status e mantém aluno/curso" \
    "$REF_FILTER"' .status == $s and ref("aluno") == $a and ref("curso") == $c' \
    --arg s "$STATUS_FINAL" --argjson a "$ALUNO_ID" --argjson c "$CURSO_ID"

request GET "/matriculas/$MATRICULA_ID"
expect_json "   ↳ novo status foi persistido (GET após PUT)" '.status == $s' --arg s "$STATUS_FINAL"

request PUT "/matriculas/$ID_INEXISTENTE" "$(matricula_json "$ALUNO_ID" "$CURSO_ID")"
expect_status "Atualizar matrícula inexistente" 404

# =============================================================================
# 6. MATRÍCULAS - Bean Validation
# =============================================================================
section "6. MATRÍCULAS - Bean Validation (espera HTTP 400)"

MAT_BASE="$(matricula_json "$ALUNO_ID" "$CURSO_ID")"

invalid "Corpo vazio {}"                       POST "/matriculas" '{}'                                                dataMatricula
invalid "dataMatricula ausente"                POST "/matriculas" "$(mut "$MAT_BASE" 'del(.dataMatricula)')"          dataMatricula
invalid "dataMatricula em formato inválido"    POST "/matriculas" "$(mut "$MAT_BASE" '.dataMatricula = "05/11/2026"')"
invalid "status ausente"                       POST "/matriculas" "$(mut "$MAT_BASE" 'del(.status)')"                 status
invalid "status em branco"                     POST "/matriculas" "$(mut "$MAT_BASE" '.status = ""')"                 status
invalid "aluno ausente"                        POST "/matriculas" "$(mut "$MAT_BASE" 'del(.aluno)')"                  aluno
invalid "curso ausente"                        POST "/matriculas" "$(mut "$MAT_BASE" 'del(.curso)')"                  curso
invalid "aluno com tipo errado (\"abc\")"      POST "/matriculas" "$(mut "$MAT_BASE" '.aluno = "abc"')"
invalid "PUT com status em branco"             PUT  "/matriculas/$MATRICULA_ID" "$(mut "$MAT_BASE" '.status = ""')"   status

# =============================================================================
# 7. PAGINAÇÃO E ORDENAÇÃO
# =============================================================================
section "7. PAGINAÇÃO E ORDENAÇÃO"

# Garante pelo menos 4 cursos para testar páginas
EXTRA_CURSOS=()
for nome in "Algoritmos Avançados" "Zoologia Aplicada"; do
    request POST "/cursos" "$(mut "$CURSO_BASE" ".nome = \"$nome\" | .cargaHoraria = $((RANDOM % 300 + 1))")"
    ID_TMP="$(json_id)"; [ -n "$ID_TMP" ] && EXTRA_CURSOS+=("$ID_TMP")
done

request GET "/cursos?page=0&size=2"
expect_status "GET /cursos?page=0&size=2" 200
expect_json "   ↳ respeita size=2 (no máximo 2 itens, size = 2, página 0)" \
    "$PAGE_FILTER"' (.content | length) <= 2 and psize == 2 and pnum == 0'
expect_json "   ↳ informa o total de elementos (>= 4)" "$PAGE_FILTER"' ptotal >= 4'

request GET "/cursos?page=1&size=2"
expect_status "GET /cursos?page=1&size=2" 200
expect_json "   ↳ retorna a página 1" "$PAGE_FILTER"' pnum == 1 and (.content | length) > 0'

request GET "/cursos?page=0&size=50&sort=nome,asc"
expect_status "GET /cursos?sort=nome,asc" 200
expect_json "   ↳ nomes em ordem crescente" '[.content[].nome] as $n | $n == ($n | sort)'

request GET "/cursos?page=0&size=50&sort=nome,desc"
expect_json "GET /cursos?sort=nome,desc -> nomes em ordem decrescente" '[.content[].nome] as $n | $n == ($n | sort | reverse)'

request GET "/cursos?page=0&size=50&sort=cargaHoraria,desc"
expect_json "GET /cursos?sort=cargaHoraria,desc -> ordenação numérica" \
    '[.content[].cargaHoraria] as $c | $c == ($c | sort | reverse)'

request GET "/cursos?page=9999&size=5"
expect_status "Página além do fim" 200
expect_json "   ↳ devolve lista vazia" '(.content | length) == 0'

request GET "/alunos?page=0&size=1&sort=nome,asc"
expect_status "GET /alunos?page=0&size=1&sort=nome,asc" 200
expect_json "   ↳ respeita size=1" "$PAGE_FILTER"' (.content | length) <= 1 and psize == 1'

request GET "/alunos?page=0&size=50&sort=nome,desc"
expect_json "GET /alunos?sort=nome,desc -> nomes em ordem decrescente" '[.content[].nome] as $n | $n == ($n | sort | reverse)'

request GET "/matriculas?page=0&size=5"
expect_status "GET /matriculas?page=0&size=5" 200
expect_json "   ↳ resposta paginada com a matrícula criada" \
    "$PAGE_FILTER"' (.content | type) == "array" and psize == 5 and ptotal >= 1'

# =============================================================================
# 8. REGRAS DE EXCLUSÃO
# =============================================================================
section "8. REGRAS DE EXCLUSÃO"

request DELETE "/cursos/$CURSO_ID"
expect_status "Regra: impedir exclusão de curso com matrículas" 400 409 422
request GET "/cursos/$CURSO_ID"
expect_status "   ↳ curso continua existindo" 200

request DELETE "/alunos/$ALUNO_ID"
expect_status "Regra: impedir exclusão de aluno com matrículas" 400 409 422
request GET "/alunos/$ALUNO_ID"
expect_status "   ↳ aluno continua existindo" 200

request DELETE "/matriculas/$MATRICULA_ID"
expect_status "Cancelar matrícula" 200 204
request GET "/matriculas/$MATRICULA_ID"
expect_status "   ↳ matrícula não existe mais" 404
request DELETE "/matriculas/$MATRICULA_ID"
expect_status "Cancelar matrícula já removida" 404

request DELETE "/cursos/$CURSO_ID"
expect_status "Excluir curso sem matrículas" 200 204
request GET "/cursos/$CURSO_ID"
expect_status "   ↳ curso não existe mais" 404

request DELETE "/alunos/$ALUNO_ID"
expect_status "Excluir aluno sem matrículas" 200 204
request GET "/alunos/$ALUNO_ID"
expect_status "   ↳ aluno não existe mais" 404

request DELETE "/cursos/$ID_INEXISTENTE"
expect_status "Excluir curso inexistente" 404

request DELETE "/alunos/$ID_INEXISTENTE"
expect_status "Excluir aluno inexistente" 404

# -----------------------------------------------------------------------------
# Limpeza dos dados auxiliares
# -----------------------------------------------------------------------------
for id in "$CURSO_INATIVO_ID" "${EXTRA_CURSOS[@]}"; do
    curl -s -o /dev/null -X DELETE "$BASE_URL/cursos/$id"
done

summary
[ $FAIL -eq 0 ]