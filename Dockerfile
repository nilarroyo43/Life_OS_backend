# ─── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar wrapper y pom primero para cachear dependencias
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente y empaquetar
COPY src ./src
RUN ./mvnw package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
