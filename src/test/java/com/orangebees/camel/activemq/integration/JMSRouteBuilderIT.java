/*
 * Copyright 2022 John Yeary<john.yeary@orangebees.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orangebees.camel.activemq.integration;

import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author John Yeary<john.yeary@orangebees.com>
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class JMSRouteBuilderIT {

    private static final Properties TEST_PROPERTIES = loadTestProperties();
    private static final int BROKER_PORT = Integer.parseInt(Objects.requireNonNull(
            TEST_PROPERTIES.getProperty("test.activemq.port"), "Missing test.activemq.port"));

    @Container
    private static final GenericContainer<?> BROKER = new GenericContainer<>(
            DockerImageName.parse(Objects.requireNonNull(
                    TEST_PROPERTIES.getProperty("test.activemq.image"), "Missing test.activemq.image")))
            .withExposedPorts(BROKER_PORT)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofSeconds(60));

    @Autowired
    private ConnectionFactory connectionFactory;

    @Value("${local.server.port}")
    private int httpPort;

    private static Properties loadTestProperties() {
        // The static container is configured before Spring creates the test context.
        try {
            return PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-test.properties"));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot load application-test.properties", e);
        }
    }

    @DynamicPropertySource
    static void brokerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.activemq.broker-url",
                () -> "tcp://" + BROKER.getHost() + ":" + BROKER.getMappedPort(BROKER_PORT)
                        + "?connectionTimeout=5000");
        registry.add("spring.activemq.user", () -> "admin");
        registry.add("spring.activemq.password", () -> "admin");
        registry.add("queue.input", () -> "jms:inputQueue");
        registry.add("queue.output", () -> "jms:outputQueue");
    }

    @Test
    @Timeout(30)
    public void processJMSRouteTest() throws Exception {
        String body = "Test Message " + UUID.randomUUID();

        try (Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageConsumer consumer = session.createConsumer(session.createQueue("outputQueue"));
                MessageProducer producer = session.createProducer(session.createQueue("inputQueue"))) {
            connection.start();
            producer.send(session.createTextMessage(body));

            Message message = consumer.receive(10000);
            assertNotNull(message, "Camel did not forward the message within 10 seconds");
            assertInstanceOf(TextMessage.class, message, "Expected a JMS text message");
            assertEquals(body, ((TextMessage) message).getText());
        }
    }

    @Test
    @Timeout(30)
    public void restEndpointsTest() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> hello = client.send(httpRequest("/say/hello").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, hello.statusCode());
            assertEquals("Hello World\n", hello.body());

            HttpResponse<String> bye = client.send(httpRequest("/say/bye")
                    .header("Content-Type", "application/json").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, bye.statusCode());
            assertEquals("Bye World\n", bye.body());

            HttpResponse<String> api = client.send(httpRequest("/api-doc").GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, api.statusCode());
            assertTrue(api.body().contains("\"/say/hello\""), "OpenAPI must describe the hello endpoint");
        }
    }

    @Test
    @Timeout(30)
    public void restToJMSRouteTest() throws Exception {
        String body = "<message>" + UUID.randomUUID() + "</message>";
        try (HttpClient client = HttpClient.newHttpClient();
                Connection connection = connectionFactory.createConnection();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                MessageConsumer consumer = session.createConsumer(session.createQueue("outputQueue"))) {
            connection.start();
            HttpResponse<String> response = client.send(httpRequest("/say/dumpster")
                    .header("Content-Type", "application/xml")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode());
            assertEquals("", response.body());

            Message message = consumer.receive(10000);
            assertInstanceOf(BytesMessage.class, message, "Expected the HTTP payload as a JMS bytes message");
            assertArrayEquals(body.getBytes(StandardCharsets.UTF_8), message.getBody(byte[].class));
        }
    }

    private HttpRequest.Builder httpRequest(String path) {
        return HttpRequest.newBuilder(URI.create("http://localhost:" + httpPort + path))
                .timeout(Duration.ofSeconds(10));
    }

}
