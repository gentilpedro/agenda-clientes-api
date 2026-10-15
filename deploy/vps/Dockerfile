# Imagem de produção da VPS (https://agenda-api.aamora.com.br). Contexto = raiz do repositório:
#   docker build -f deploy/vps/Dockerfile --build-arg VERSAO=1.2.3 -t agenda-api:1.2.3 .
# O Dockerfile da raiz continua sendo o do Render.
FROM maven:3.9-eclipse-temurin-21 AS build
ARG VERSAO=0.0.0
WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
# Grava a versão da release no jar (aparece em app.version), sem commitar no pom.
RUN mvn -B -q versions:set -DnewVersion=$VERSAO -DgenerateBackupPoms=false \
 && mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S -G app -u 1654 app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8080
# Heap proporcional ao mem_limit do container (docker-compose.yml).
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENTRYPOINT ["java", "-jar", "app.jar"]
