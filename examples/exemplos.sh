#!/usr/bin/env bash
#
# Exemplos de uso da API Cerberus via curl.
# Pré-requisito: o serviço rodando em http://localhost:8080
# (mvn spring-boot:run  OU  java -jar target/cerberus-1.0.0.jar).
#
set -euo pipefail
BASE="${BASE:-http://localhost:8080}"

echo "== 1) Análise de força — senha fraca =="
curl -s -X POST "$BASE/api/v1/password/strength" \
  -H 'Content-Type: application/json' \
  -d '{"password":"password"}'
echo; echo

echo "== 2) Análise de força — senha forte =="
curl -s -X POST "$BASE/api/v1/password/strength" \
  -H 'Content-Type: application/json' \
  -d '{"password":"9xQ!vTm2#Lp8zR"}'
echo; echo

echo "== 3) Verificação de vazamento (HaveIBeenPwned, k-anonymity) =="
curl -s -X POST "$BASE/api/v1/password/breach" \
  -H 'Content-Type: application/json' \
  -d '{"password":"password"}'
echo; echo

echo "== 4) Auditoria completa contra a política =="
curl -s -X POST "$BASE/api/v1/password/policy" \
  -H 'Content-Type: application/json' \
  -d '{"password":"Zx9q!Lm3#Tp7wRk2vBn8"}'
echo; echo

echo "== 5) Gerar hash BCrypt =="
HASH=$(curl -s -X POST "$BASE/api/v1/hash/bcrypt" \
  -H 'Content-Type: application/json' \
  -d '{"password":"MinhaSenh@1"}' | sed -E 's/.*"hash":"([^"]+)".*/\1/')
echo "hash gerado: $HASH"
echo

echo "== 6) Verificar senha contra o hash =="
curl -s -X POST "$BASE/api/v1/hash/verify" \
  -H 'Content-Type: application/json' \
  -d "{\"password\":\"MinhaSenh@1\",\"hash\":\"$HASH\"}"
echo
