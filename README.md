# Cerberus 🔐

**Serviço REST de auditoria de segurança de senhas** — análise de força, verificação de vazamento (HaveIBeenPwned via *k-anonymity*) e hashing seguro (BCrypt), em um único artefato.

> Artefato desenvolvido para a atividade de Cibersegurança (UNIPAMPA), com apoio de Inteligência Artificial. O relato completo do processo assistido por IA está em [`RELATO.txt`](RELATO.txt).

---

## 1. Descrição do problema

Senhas continuam sendo o principal mecanismo de autenticação na maioria dos sistemas — e também um dos elos mais frágeis. Três falhas recorrentes de higiene de senhas causam grande parte dos incidentes:

1. **Senhas fracas ou previsíveis** (curtas, sem variedade de caracteres, baseadas em palavras comuns ou padrões de teclado como `qwerty`/`123456`).
2. **Reutilização de senhas já vazadas** em incidentes públicos, que alimentam ataques de *credential stuffing*.
3. **Armazenamento inadequado** de senhas pelas aplicações (texto claro, MD5/SHA sem *salt*), que transforma um vazamento de banco de dados em comprometimento imediato de contas.

Não existe, para muitas equipes pequenas, uma forma simples e centralizada de **verificar programaticamente** esses três pontos ao cadastrar ou auditar senhas.

## 2. Motivação

Bibliotecas isoladas resolvem partes do problema, mas obrigam cada aplicação a integrar e manter várias dependências (medidor de força, cliente HIBP, biblioteca de hashing) e a acertar detalhes sensíveis — por exemplo, **nunca** enviar a senha em texto claro a um serviço externo. Cerberus encapsula essas boas práticas atrás de uma **API HTTP simples e reutilizável**, para que qualquer aplicação (em qualquer linguagem) possa consumir a auditoria de senhas sem reimplementar a lógica de segurança.

## 3. Objetivo do artefato

Fornecer um **serviço executável e reutilizável** que, dado uma senha, responda de forma acionável:

- Quão forte ela é (score + entropia + diagnóstico);
- Se já apareceu em vazamentos conhecidos, **sem expor a senha** (modelo *k-anonymity*);
- Se está em conformidade com uma **política configurável**;
- E que demonstre o **armazenamento correto** de senhas via BCrypt (gerar e verificar hash).

## 4. Principais funcionalidades

| Endpoint | Método | Descrição |
|---|---|---|
| `/api/v1/password/strength` | POST | Analisa a força: entropia estimada, classes de caracteres, senhas comuns, sequências de teclado e repetições. Retorna score (0–4), rótulo, avisos e sugestões. |
| `/api/v1/password/breach` | POST | Verifica se a senha aparece na base [Pwned Passwords](https://haveibeenpwned.com/Passwords) usando **k-anonymity** (só os 5 primeiros caracteres do SHA-1 saem do servidor). |
| `/api/v1/password/policy` | POST | **Auditoria completa**: avalia a senha contra a política configurada (tamanho mínimo, classes exigidas, proibição de senhas comuns e vazadas) e retorna conformidade + violações. |
| `/api/v1/hash/bcrypt` | POST | Gera um hash BCrypt (com *salt* aleatório e *work factor* 12). |
| `/api/v1/hash/verify` | POST | Verifica, em tempo constante, se uma senha corresponde a um hash BCrypt. |
| `/actuator/health` | GET | Health check do serviço. |
| `/docs` | GET | Documentação interativa (Swagger UI). |

**Destaques de segurança do próprio artefato:**
- A senha **nunca** é registrada em log nem persistida.
- A verificação de vazamento usa *k-anonymity* + cabeçalho `Add-Padding` — a senha não trafega para o serviço externo.
- Degradação graciosa: se a API de vazamentos estiver indisponível, o serviço responde `checked=false` em vez de falhar.

## 5. Dependências

Gerenciadas via Maven (ver [`pom.xml`](pom.xml)):

- **Spring Boot 3.3.5** (`spring-boot-starter-web`, `-validation`, `-actuator`)
- **spring-security-crypto** — `BCryptPasswordEncoder`
- **springdoc-openapi 2.6.0** — Swagger UI / OpenAPI
- **spring-boot-starter-test** (JUnit 5, MockMvc, Mockito, AssertJ) — apenas para testes

Nenhuma dependência precisa ser instalada manualmente: o Maven as resolve automaticamente.

## 6. Requisitos de execução

Escolha **uma** das opções:

- **Execução local:** apenas **JDK 21+** (`java -version`). O Maven **não** precisa estar instalado — o projeto inclui o *Maven Wrapper* (`./mvnw`), que baixa a versão correta do Maven automaticamente.
- **Via Docker:** apenas Docker (o build acontece dentro do container).

> A funcionalidade de **verificação de vazamento** requer acesso de saída HTTPS a `api.pwnedpasswords.com`. As demais funcionalidades operam totalmente offline.

## 7. Instalação

```bash
# Clonar o repositório
git clone https://github.com/AndreLGDM/Cerberus.git
cd Cerberus

# Compilar e empacotar (roda os testes) — usando o Maven Wrapper
./mvnw clean package        # no Windows: mvnw.cmd clean package
```

Isso produz o artefato executável em `target/cerberus-1.0.0.jar`.

## 8. Execução

**Opção A — Maven Wrapper (desenvolvimento):**
```bash
./mvnw spring-boot:run
```

**Opção B — JAR empacotado:**
```bash
java -jar target/cerberus-1.0.0.jar
```

**Opção C — Docker:**
```bash
docker build -t cerberus:1.0.0 .
docker run --rm -p 8080:8080 cerberus:1.0.0
# ou:
docker compose up --build
```

O serviço sobe em `http://localhost:8080`. Documentação interativa em `http://localhost:8080/docs`.

**Configuração (opcional)** — a política pode ser ajustada por variáveis de ambiente, sem recompilar:
```bash
CERBERUS_POLICY_MIN_LENGTH=16 CERBERUS_POLICY_REQUIRE_SYMBOL=true java -jar target/cerberus-1.0.0.jar
```

## 9. Exemplos de uso

> Script pronto com todos os exemplos: [`examples/exemplos.sh`](examples/exemplos.sh). Coleção para IntelliJ/VS Code: [`examples/requests.http`](examples/requests.http).

**Análise de força (senha fraca):**
```bash
curl -s -X POST http://localhost:8080/api/v1/password/strength \
  -H 'Content-Type: application/json' -d '{"password":"password"}'
```
```json
{
  "score": 0,
  "rating": "MUITO_FRACA",
  "entropyBits": 37.6,
  "length": 8,
  "hasLowercase": true, "hasUppercase": false, "hasDigit": false, "hasSymbol": false,
  "warnings": ["Senha presente em listas de senhas mais comuns."],
  "suggestions": ["Use pelo menos 12 caracteres (idealmente 16 ou mais).", "Adicione letras maiúsculas.", "Adicione dígitos.", "Adicione símbolos (ex.: !@#$%)."]
}
```

**Análise de força (senha forte):**
```bash
curl -s -X POST http://localhost:8080/api/v1/password/strength \
  -H 'Content-Type: application/json' -d '{"password":"9xQ!vTm2#Lp8zR"}'
```
```json
{ "score": 3, "rating": "FORTE", "entropyBits": 91.98, "length": 14, "warnings": [], ... }
```

**Auditoria completa contra a política:**
```bash
curl -s -X POST http://localhost:8080/api/v1/password/policy \
  -H 'Content-Type: application/json' -d '{"password":"Zx9q!Lm3#Tp7wRk2vBn8"}'
```
```json
{ "compliant": true, "violations": [], "strength": { "score": 4, "rating": "MUITO_FORTE", ... }, "breach": { ... } }
```

**Hashing seguro (gerar e verificar):**
```bash
# Gera o hash
curl -s -X POST http://localhost:8080/api/v1/hash/bcrypt \
  -H 'Content-Type: application/json' -d '{"password":"MinhaSenh@1"}'
# {"algorithm":"bcrypt","hash":"$2a$12$..."}

# Verifica
curl -s -X POST http://localhost:8080/api/v1/hash/verify \
  -H 'Content-Type: application/json' -d '{"password":"MinhaSenh@1","hash":"$2a$12$..."}'
# {"matches":true}
```

## 10. Estrutura do repositório

```
cerberus/
├── pom.xml                        # Configuração Maven e dependências
├── mvnw, mvnw.cmd, .mvn/          # Maven Wrapper (dispensa Maven instalado)
├── .github/workflows/ci.yml       # Integração contínua (build + testes)
├── Dockerfile                     # Build multi-stage + execução como usuário não-root
├── .dockerignore
├── docker-compose.yml             # Orquestração de exemplo
├── LICENSE                        # Licença MIT
├── README.md                      # Este arquivo
├── RELATO.txt                     # Relato do processo assistido por IA
├── examples/
│   ├── exemplos.sh                # Exemplos de uso via curl
│   └── requests.http              # Coleção de requisições HTTP
└── src/
    ├── main/
    │   ├── java/br/edu/unipampa/cerberus/
    │   │   ├── CerberusApplication.java
    │   │   ├── config/            # Propriedades da política/breach, OpenAPI
    │   │   ├── controller/        # PasswordController, HashController
    │   │   ├── dto/               # Records de requisição/resposta
    │   │   ├── exception/         # Tratamento global de erros
    │   │   └── service/           # Força, vazamento (k-anonymity), hash, política
    │   └── resources/
    │       ├── application.yml    # Configuração (porta, política, breach)
    │       └── common-passwords.txt
    └── test/
        └── java/br/edu/unipampa/cerberus/
            ├── controller/PasswordApiIntegrationTest.java
            └── service/           # Testes unitários dos 4 serviços
```

## 11. Testes

O projeto tem **23 testes** (unitários + integração de API) cobrindo os quatro serviços e as rotas HTTP, incluindo validação de entrada e degradação graciosa da verificação de vazamento.

```bash
./mvnw test
```
Saída esperada (resumo):
```
Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Destaques da estratégia de teste:
- `BreachCheckServiceTest` valida o SHA-1 contra um vetor conhecido, confirma que **apenas o prefixo de 5 caracteres** é enviado (garantia de *k-anonymity*) e cobre o caminho de indisponibilidade da API.
- `PasswordApiIntegrationTest` sobe o contexto Spring com `MockMvc` e um cliente HIBP *mockado* — os testes **não dependem de rede**.

## 12. Limitações conhecidas

- **Dependência de rede para a verificação de vazamento.** O endpoint `/breach` (e a checagem de vazamento dentro de `/policy`) requer acesso a `api.pwnedpasswords.com`. Em ambientes sem esse acesso (por exemplo, o sandbox de avaliação usado no desenvolvimento, cuja política de egresso bloqueia o domínio), o serviço retorna `checked=false` com mensagem explicativa, em vez de falhar. As demais funcionalidades continuam operando normalmente.
- **Dicionário de senhas comuns reduzido.** [`common-passwords.txt`](src/main/resources/common-passwords.txt) contém ~140 entradas para fins didáticos. Em produção, recomenda-se substituí-lo por uma lista maior (ex.: *SecLists*).
- **Estimativa de entropia é um limite superior.** A fórmula `comprimento × log2(alfabeto)` não modela toda a previsibilidade linguística; por isso são aplicadas heurísticas de penalização. Não substitui um analisador estatístico completo como o *zxcvbn*.
- **Sem autenticação/rate limiting.** O serviço é pensado para uso interno/didático. Exposição pública exigiria autenticação, limitação de taxa e HTTPS terminado por um *reverse proxy*.
- **Apenas BCrypt.** Argon2/scrypt não estão incluídos para manter o escopo enxuto; a arquitetura permite adicioná-los facilmente.

## 13. Mapeamento aos critérios de avaliação de artefatos

- **Disponibilidade:** repositório público no GitHub, licença MIT, sem dependências proprietárias.
- **Funcionalidade:** API REST operacional com 6 endpoints, documentação OpenAPI e tratamento de erros; comportamento validado por 23 testes automatizados e por testes manuais (`curl`) descritos acima.
- **Sustentabilidade:** código em camadas (controller/service/dto/config), responsabilidades isoladas, política externalizada em configuração, dependência HTTP abstraída atrás de interface e documentação (Javadoc + README).
- **Reprodutibilidade:** *Maven Wrapper* (`./mvnw`) fixa a versão do Maven e dispensa instalação prévia; versões de dependências fixadas; `Dockerfile` multi-stage que compila e executa sem depender do host; workflow de CI que valida o build em ambiente limpo; e testes que rodam sem rede.

## 14. Referências

- OWASP. *Password Storage Cheat Sheet.* https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
- OWASP. *Authentication Cheat Sheet.* https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
- NIST SP 800-63B — *Digital Identity Guidelines: Authentication and Lifecycle Management.* https://pages.nist.gov/800-63-3/sp800-63b.html
- Hunt, Troy. *Have I Been Pwned — Pwned Passwords (k-Anonymity API).* https://haveibeenpwned.com/API/v3#PwnedPasswords
- Provos, N.; Mazières, D. *A Future-Adaptable Password Scheme (bcrypt).* USENIX, 1999.
- Wheeler, D. *zxcvbn: Low-Budget Password Strength Estimation.* USENIX Security, 2016.
- Documentação de avaliação de artefatos — SBSeg 2026. https://doc-artefatos.github.io/sbseg2026/

## 15. Licença

Distribuído sob a licença **MIT**. Veja [`LICENSE`](LICENSE).
