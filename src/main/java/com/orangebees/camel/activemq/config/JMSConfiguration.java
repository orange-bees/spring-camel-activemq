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
package com.orangebees.camel.activemq.config;

import org.apache.camel.component.jms.JmsComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.JmsTransactionManager;

import jakarta.jms.ConnectionFactory;

/**
 * Add JMS specific configuration based on Spring Boot provided
 * {@link ConnectionFactory}.
 *
 * @author John Yeary<john.yeary@orangebees.com>
 * @version 1.0.0
 */
@Configuration
public class JMSConfiguration {

    /**
     * Configure a {@link JmsTransactionManager} for use with the
     * {@link JmsComponent}.
     *
     * @param connectionFactory The {@link ConnectionFactory} to use.
     * @return A configured {@link JmsTransactionManager}.
     */
    @Bean
    public JmsTransactionManager jmsTransactionManager(final ConnectionFactory connectionFactory) {
        JmsTransactionManager jmsTransactionManager = new JmsTransactionManager();
        jmsTransactionManager.setConnectionFactory(connectionFactory);
        return jmsTransactionManager;
    }

    /**
     * Configure a {@link JmsComponent} for use with the {@link JmsTransactionManager}.
     *
     * @param connectionFactory     The {@link ConnectionFactory} to use.
     * @param jmsTransactionManager The {@link JmsTransactionManager} to use.
     * @return A configured {@link JmsComponent}.
     */
    @Bean
    public JmsComponent jmsComponent(final ConnectionFactory connectionFactory, final JmsTransactionManager jmsTransactionManager) {
        return JmsComponent
                .jmsComponentTransacted(connectionFactory, jmsTransactionManager);
    }

}
