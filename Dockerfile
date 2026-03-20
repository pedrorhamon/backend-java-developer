# ── Stage 1: Build com Maven ──────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /app

# Instala Maven diretamente — sem depender do mvnw ou da pasta .mvn/
ARG MAVEN_VERSION=3.9.9
RUN apk add --no-cache curl tar && \
    curl -fsSL https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    | tar -xz -C /opt && \
    ln -s /opt/apache-maven-${MAVEN_VERSION}/bin/mvn /usr/local/bin/mvn

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
