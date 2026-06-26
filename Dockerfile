# =============================================================================
# Stage 1: Build the application with Maven
# =============================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# Cache dependencies separately from source so code-only changes don't
# re-download the entire dependency tree on every build.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# =============================================================================
# Stage 2: Minimal runtime image
# =============================================================================
FROM eclipse-temurin:21-jre-alpine-3.23 AS runtime

# Run as a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=build /build/target/academic-advisor-*.jar app.jar

RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

# Reasonable JVM defaults for a small/medium container; override via JAVA_OPTS
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
