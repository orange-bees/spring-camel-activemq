# spring-camel-activemq

This is an example application for demonstrating Spring Boot and Camel with Apache MQ.

The example is highly configurable by modifying the `application.properties` file to change any of its parameters.

## Requirements

The application targets Java 25 LTS and uses Spring Boot 4.1.2 (Spring Framework 7)
and Apache Camel 4.23.0. Build and run it with JDK 25 and Maven 3.6.3 or newer.
Ensure that Maven's `JAVA_HOME` (or selected `jenv` version) points to JDK 25.
JMS integration uses the Jakarta JMS API and the ActiveMQ Classic client.

## Integration Tests

The integration test requires JDK 25, Maven, and a running Docker-compatible
container runtime accessible to Testcontainers.

Run `mvn verify`, or run `JMSRouteBuilderIT` directly as a JUnit test in the IDE.
Testcontainers starts an isolated ActiveMQ Classic broker, waits for its JMS port,
and supplies its dynamically mapped host address to Spring Boot. The application
runs in the test JVM with a random HTTP port; no fixed host ports or separately
started application or broker are needed. The test verifies that Camel forwards
the expected text message from `inputQueue` to `outputQueue`, the REST greeting
endpoints and OpenAPI document, and XML forwarding from REST through JMS.

Configure the broker image and internal JMS port with `test.activemq.image` and
`test.activemq.port` in `src/test/resources/application-test.properties`. The test
loads these values before creating the container and activates the Spring `test`
profile. The port must match the selected image's JMS listener; it does not change
the broker's listener configuration. The host port remains randomly allocated.

The broker is stopped automatically after the tests. Docker must be available;
the integration test fails rather than silently skipping when it is unavailable.
This tests the Spring application and a real broker, not the packaged application
Docker image.

## Running application 

This requires an Apache MQ Server running separately. You may choose to use a pre-built Docker image as shown below:

```bash
docker container run -p 61616:61616 -p 8161:8161 --rm rmohr/activemq:5.15.9     
```

## Building the application image

Build from the repository root:

```bash
docker build --pull -f src/test/resources/Dockerfile -t spring-camel-activemq .
```

The builder uses `maven:3-eclipse-temurin-25`, tracking the latest Maven 3 image
with JDK 25. Only the executable JAR is copied into the Zulu 25 runtime image;
Maven and the source tree are not included. The image build skips tests because
the integration tests require Docker; run `mvn verify` separately.

Run the application with the address of an external ActiveMQ Classic broker:

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_ACTIVEMQ_BROKER_URL=tcp://host.docker.internal:61616 \
  spring-camel-activemq
```

`host.docker.internal` addresses the host on Docker Desktop. For a broker on a
Docker network, attach the application to that network and use the broker's
container name instead. Set `SPRING_ACTIVEMQ_USER` and `SPRING_ACTIVEMQ_PASSWORD`
when the broker requires different credentials.
