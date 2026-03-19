# ── Stage 1: Build com Maven ──────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# Copia o wrapper e pom primeiro (cache de dependências)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copia o código-fonte e faz o build
COPY src/ src/
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime leve ─────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9012

ENTRYPOINT ["java", "-jar", "app.jar"]
