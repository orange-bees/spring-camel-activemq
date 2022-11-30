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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author John Yeary<john.yeary@orangebees.com>
 */
@Slf4j
public class JMSRouteBuilderIT {

    private ConnectionFactory connectionFactory;

    public JMSRouteBuilderIT() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        connectionFactory = new ActiveMQConnectionFactory();
    }

    @AfterEach
    public void tearDown() {
    }

    private void jmsMessageProducer() throws Exception {

        try ( Connection connection = connectionFactory.createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("inputQueue");
            MessageProducer messageProducer = session.createProducer(destination);
            Message msg = session.createTextMessage("Test Message");
            messageProducer.send(msg);
            log.info("JMS Message Sent");
        }
    }

    private Message jmsMessageConsumer() throws Exception {
        Message message = null;

        try ( Connection connection = connectionFactory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue("outputQueue");
            MessageConsumer messageConsumer = session.createConsumer(destination);
            message = messageConsumer.receive(1000);
            session.close();
        }

        return message;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void processJMSRouteTest() throws Exception {
        jmsMessageProducer();
        assertNotNull(jmsMessageConsumer());
    }

}
