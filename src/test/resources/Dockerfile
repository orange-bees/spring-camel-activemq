FROM azul/zulu-openjdk-alpine:18.0.2.1
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar","/app.jar"]