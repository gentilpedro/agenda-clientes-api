# Build: compila com Maven num estágio separado, só o jar final vai pra imagem
# de runtime — mantém a imagem publicada pequena e sem toolchain de build.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Plataformas como o Render injetam a porta real via $PORT; server.port no
# application.properties usa ${PORT:8080} como fallback pro dev local.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
