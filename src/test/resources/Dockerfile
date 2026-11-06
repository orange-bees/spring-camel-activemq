FROM maven:3-eclipse-temurin-25 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp -DskipTests package

FROM azul/zulu-openjdk-alpine:25.0.5
WORKDIR /app
COPY --from=builder /build/target/spring-camel-activemq.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","/app.jar"]