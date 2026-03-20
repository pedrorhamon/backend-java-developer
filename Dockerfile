# ── Stage 1: Build com Maven ──────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# Instala Maven pelo repositório do Alpine — sem dependência de URL externa
RUN apk add --no-cache maven

# Copia apenas o pom primeiro para aproveitar o cache de dependências
COPY pom.xml ./
RUN mvn dependency:go-offline -q

# Copia o código-fonte e faz o build
COPY src/ src/
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime leve ─────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9012

ENTRYPOINT ["java", "-jar", "app.jar"]
