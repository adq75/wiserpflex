FROM maven:3.10.1-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy only what we need first to leverage Docker layer cache
COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=target/*.jar
COPY --from=builder /workspace/target/*.jar /app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
