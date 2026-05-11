# Build stage
FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY settings.xml /usr/share/maven/ref/settings-docker.xml
COPY src ./src
RUN mvn -s /usr/share/maven/ref/settings-docker.xml package -DskipTests

# Run stage
FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "app.jar"]
