/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.jms;

import java.util.Map;

import org.apache.camel.Headers;
import org.apache.camel.test.spring.junit5.CamelSpringTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 */
public class SpringJmsRoutingSlipInOutTest extends CamelSpringTestSupport {

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/component/jms/SpringJmsRoutingSlipInOutTest.xml");
    }

    @Test
    public void testInOutRoutingSlip() throws Exception {
        getMockEndpoint("mock:result").expectedBodiesReceived("Result-Done-B-A-Hello");

        template.sendBody("activemq:queue:SpringJmsRoutingSlipInOutTest.start", "Hello");

        assertMockEndpointsSatisfied();
    }

    public static final class MyBean {

        public void createSlip(@Headers Map<String, Object> headers) {
            headers.put("mySlip",
                    "activemq:queue:SpringJmsRoutingSlipInOutTest.a,activemq:queue:SpringJmsRoutingSlipInOutTest.b");
        }

        public String backFromSlip(String body) {
            return "Done-" + body;
        }

        public String doA(String body) {
            return "A-" + body;
        }

        public String doB(String body) {
            return "B-" + body;
        }

        public String doResult(String body) {
            return "Result-" + body;
        }
    }

}
