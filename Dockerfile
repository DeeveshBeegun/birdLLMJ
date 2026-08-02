# syntax=docker/dockerfile:1

# --- Build stage ---
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# The wrapper may lose its execute bit when committed from Windows; restore it for the Linux build.
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app
# Run as a non-root user.
RUN useradd -r -u 1001 wildlife
COPY --from=build /workspace/target/*.jar /app/application.jar
USER wildlife
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/application.jar"]
