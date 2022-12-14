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
package com.orangebees.camel.activemq.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * An example processor that logs the message body.
 *
 * @author John Yeary<john.yeary@orangebees.com>
 * @version 1.0.0
 */
@Slf4j
public class MessageProcessor implements Processor {

    public MessageProcessor() {
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        log.info("Message Body: {}", exchange.getIn().getBody(String.class));
    }

}
