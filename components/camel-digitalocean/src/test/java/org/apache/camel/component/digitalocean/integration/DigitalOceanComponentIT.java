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
package org.apache.camel.component.digitalocean.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.myjeeva.digitalocean.pojo.Account;
import com.myjeeva.digitalocean.pojo.Action;
import com.myjeeva.digitalocean.pojo.Droplet;
import com.myjeeva.digitalocean.pojo.Image;
import com.myjeeva.digitalocean.pojo.Region;
import com.myjeeva.digitalocean.pojo.Size;
import com.myjeeva.digitalocean.pojo.Tag;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.digitalocean.constants.DigitalOceanHeaders;
import org.apache.camel.component.digitalocean.constants.DigitalOceanOperations;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.apache.camel.test.junit5.TestSupport.assertCollectionSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf(value = "org.apache.camel.component.digitalocean.integration.DigitalOceanTestSupport#hasCredentials",
           disabledReason = "Must be manually tested. Provide your own oAuthToken on test-options.properties")
public class DigitalOceanComponentIT extends DigitalOceanTestSupport {

    @EndpointInject("mock:result")
    protected MockEndpoint mockResultEndpoint;

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from("direct:getAccountInfo")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.get))
                        .to("digitalocean:account?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getAccountInfo2")
                        .to("digitalocean:account?operation=" + DigitalOceanOperations.get + "&oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getActions")
                        .to("digitalocean:actions?operation=list&oAuthToken={{oAuthToken}}&perPage=30")
                        .to("mock:result");

                from("direct:getActionInfo")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.get))
                        .setHeader(DigitalOceanHeaders.ID, constant(133459716))
                        .to("digitalocean:actions?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getDroplets")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.list))
                        .to("digitalocean:droplets?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getDroplet")
                        .setHeader(DigitalOceanHeaders.ID, constant(5428878))
                        .to("digitalocean:droplets?operation=get&oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getDroplet2")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.get))
                        .setHeader(DigitalOceanHeaders.ID, constant(5428878))
                        .to("digitalocean:droplets?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:createDroplet")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.create))
                        .setHeader(DigitalOceanHeaders.NAME, constant("camel-test"))
                        .setHeader(DigitalOceanHeaders.REGION, constant("fra1"))
                        .setHeader(DigitalOceanHeaders.DROPLET_IMAGE, constant("ubuntu-14-04-x64"))
                        .setHeader(DigitalOceanHeaders.DROPLET_SIZE, constant("512mb"))
                        .process(e -> {
                            Collection<String> tags = new ArrayList<>();
                            tags.add("tag1");
                            tags.add("tag2");
                            e.getIn().setHeader(DigitalOceanHeaders.DROPLET_TAGS, tags);

                        })
                        .to("digitalocean:droplets?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:createMultipleDroplets")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.create))
                        .process(e -> {
                            Collection<String> names = new ArrayList<>();
                            names.add("droplet1");
                            names.add("droplet2");
                            e.getIn().setHeader(DigitalOceanHeaders.NAMES, names);

                        })
                        .setHeader(DigitalOceanHeaders.REGION, constant("fra1"))
                        .setHeader(DigitalOceanHeaders.DROPLET_IMAGE, constant("ubuntu-14-04-x64"))
                        .setHeader(DigitalOceanHeaders.DROPLET_SIZE, constant("512mb"))
                        .process(e -> {
                            Collection<String> tags = new ArrayList<>();
                            tags.add("tag1");
                            tags.add("tag2");
                            e.getIn().setHeader(DigitalOceanHeaders.DROPLET_TAGS, tags);

                        })
                        .to("digitalocean://droplets?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getDropletBackups")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.listBackups))
                        .setHeader(DigitalOceanHeaders.ID, constant(5428878))
                        .to("digitalocean://droplets?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:createTag")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.create))
                        .setHeader(DigitalOceanHeaders.NAME, constant("tag1"))
                        .to("digitalocean://tags?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getTags")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.list))
                        .to("digitalocean://tags?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getImages")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.list))
                        .to("digitalocean://images?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getImage")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.get))
                        .setHeader(DigitalOceanHeaders.DROPLET_IMAGE, constant("ubuntu-14-04-x64"))
                        .to("digitalocean://images?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getSizes")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.list))
                        .to("digitalocean://sizes?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getSize")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.get))
                        .setHeader(DigitalOceanHeaders.NAME, constant("512mb"))
                        .to("digitalocean://sizes?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getRegions")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.list))
                        .to("digitalocean://regions?oAuthToken={{oAuthToken}}")
                        .to("mock:result");

                from("direct:getRegion")
                        .setHeader(DigitalOceanHeaders.OPERATION, constant(DigitalOceanOperations.get))
                        .setHeader(DigitalOceanHeaders.NAME, constant("nyc1"))
                        .to("digitalocean://regions?oAuthToken={{oAuthToken}}")
                        .to("mock:result");
            }
        };
    }

    @Test
    public void testGetAccountInfo() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(2);

        Exchange exchange = template.request("direct:getAccountInfo", null);
        assertTrue(((Account) exchange.getMessage().getBody()).isEmailVerified());
        exchange = template.request("direct:getAccountInfo2", null);
        assertTrue(((Account) exchange.getMessage().getBody()).isEmailVerified());

        assertMockEndpointsSatisfied();

    }

    @Test
    public void testGetAllActions() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getActions", null);

        assertMockEndpointsSatisfied();
        assertEquals(30, ((List) exchange.getMessage().getBody()).size());
    }

    @Test
    public void testGetActionInfo() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getActionInfo", null);

        assertMockEndpointsSatisfied();
        assertEquals(((Action) exchange.getMessage().getBody()).getId(), Integer.valueOf(133459716));
    }

    @Test
    public void testGetDropletInfo() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(2);

        Exchange exchange = template.request("direct:getDroplet", null);

        assertEquals(((Droplet) exchange.getMessage().getBody()).getId(), Integer.valueOf(5428878));

        exchange = template.request("direct:getDroplet2", null);

        assertMockEndpointsSatisfied();
        assertEquals(((Droplet) exchange.getMessage().getBody()).getId(), Integer.valueOf(5428878));

    }

    @Test
    public void testCreateDroplet() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:createDroplet", null);

        assertMockEndpointsSatisfied();
        Droplet droplet = (Droplet) exchange.getMessage().getBody();

        assertNotNull(droplet.getId());
        assertEquals("fra1", droplet.getRegion().getSlug());
        assertCollectionSize(droplet.getTags(), 2);

    }

    @Test
    public void testCreateMultipleDroplets() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:createMultipleDroplets", null);

        assertMockEndpointsSatisfied();
        List<Droplet> droplets = (List<Droplet>) exchange.getMessage().getBody();

        assertCollectionSize(droplets, 2);
    }

    @Test
    public void testGetAllDroplets() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getDroplets", null);

        assertMockEndpointsSatisfied();
        assertEquals(1, ((List) exchange.getMessage().getBody()).size());
    }

    @Test
    public void testGetDropletBackups() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getDropletBackups", null);

        assertMockEndpointsSatisfied();
        assertCollectionSize((List) exchange.getMessage().getBody(), 0);
    }

    @Test
    public void testCreateTag() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:createTag", null);

        assertMockEndpointsSatisfied();
        assertEquals("tag1", ((Tag) exchange.getMessage().getBody()).getName());
    }

    @Test
    public void testGetTags() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getTags", null);

        assertMockEndpointsSatisfied();
        assertEquals("tag1", ((List<Tag>) exchange.getMessage().getBody()).get(0).getName());
    }

    @Test
    public void getImages() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getImages", null);

        assertMockEndpointsSatisfied();
        List<Image> images = (List<Image>) exchange.getMessage().getBody();
        assertNotEquals(1, images.size());
    }

    @Test
    public void getImage() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getImage", null);

        assertMockEndpointsSatisfied();
        assertEquals("ubuntu-14-04-x64", (exchange.getMessage().getBody(Image.class)).getSlug());

    }

    @Test
    public void getSizes() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getSizes", null);

        assertMockEndpointsSatisfied();
        List<Size> sizes = (List<Size>) exchange.getMessage().getBody();
        System.out.println(sizes);
        assertNotEquals(1, sizes.size());
    }

    @Test
    public void getRegions() throws Exception {
        mockResultEndpoint.expectedMinimumMessageCount(1);

        Exchange exchange = template.request("direct:getRegions", null);

        assertMockEndpointsSatisfied();
        List<Region> regions = (List<Region>) exchange.getMessage().getBody();
        System.out.println(regions);
        assertNotEquals(1, regions.size());
    }

}
