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
package com.orangebees.camel.activemq.route;

import com.orangebees.camel.activemq.processor.MessageProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * A JMS Route that takes a message from one queue, processes it, and places it
 * in another queue.
 *
 * @author John Yeary<john.yeary@orangebees.com>
 * @version 1.0.0
 */
@Component
@Slf4j
public class JMSRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("{{queue.input}}")
                .routeId(JMSRouteBuilder.class.getSimpleName())
                .id("{{queue.input}}")
                .log(LoggingLevel.INFO, log, "Message received from {{queue.input}}")
                .id("{{{queue.input}} logger")
                .process(new MessageProcessor())
                .id(MessageProcessor.class.getSimpleName())
                .to("{{queue.output}}")
                .id("{{queue.output}}")
                .log(LoggingLevel.INFO, log, "Message successfully sent to the {{queue.output}}")
                .id("{{{queue.output}} logger")
                .end();
    }

}
