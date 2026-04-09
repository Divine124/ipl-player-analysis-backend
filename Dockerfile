# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom first (layer-cache dependencies separately)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ipl-player-analysis-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx400m", "-Xms200m", "-jar", "app.jar"]
