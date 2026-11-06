# spring-camel-activemq

This is an example application for demonstrating Spring Boot and Camel with Apache MQ.

The example is highly configurable by modifying the `application.properties` file to change any of its parameters.

## Integration Tests

The integration test requires JDK 11 or newer, Maven, and a running Docker-compatible
container runtime accessible to Testcontainers.

Run `mvn verify`, or run `JMSRouteBuilderIT` directly as a JUnit test in the IDE.
Testcontainers starts an isolated ActiveMQ Classic broker, waits for its JMS port,
and supplies its dynamically mapped host address to Spring Boot. The application
runs in the test JVM with a random HTTP port; no fixed host ports or separately
started application or broker are needed. The test verifies that Camel forwards
the expected text message from `inputQueue` to `outputQueue`.

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
docker run -p 61616:61616 -p 8161:8161 rmohr/activemq:5.15.9     
```
