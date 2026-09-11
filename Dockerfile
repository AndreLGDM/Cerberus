# ---- Estágio de build ----
# Compila o artefato dentro do container, garantindo reprodutibilidade
# independentemente da máquina host (só é preciso Docker).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Copia o pom primeiro para aproveitar o cache de dependências do Docker.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Estágio de execução ----
# Imagem final enxuta, apenas com o JRE e o jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
# curl é usado pelo HEALTHCHECK; instala-se e cria-se um usuário sem privilégios
# (boa prática de hardening de container).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -r -u 1001 cerberus
COPY --from=build /app/target/cerberus-*.jar app.jar
USER cerberus
EXPOSE 8080
# Health check usando o endpoint do Actuator.
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD ["sh", "-c", "curl -fsS http://localhost:8080/actuator/health || exit 1"]
ENTRYPOINT ["java", "-jar", "app.jar"]
