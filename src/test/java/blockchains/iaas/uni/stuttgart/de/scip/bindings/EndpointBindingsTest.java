/*******************************************************************************
 * Copyright (c) 2022 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.scip.bindings;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EndpointBindingsTest {

    @Test
    void addInvalidRule() {
        String url = "http://test.com";
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            EndpointBindings.getInstance().addRule(url, "abc");
        });
    }

    @Test
    void addSimpleRule() {
        String url = "http://test.com";
        EndpointBindings.getInstance().addRule(url, "camunda");
        String id = EndpointBindings.getInstance().getBindingIdentifierForEndpoint(url);
        Assertions.assertEquals("camunda", id);
    }

    @Test
    void addRuleWithWildcards() {
        String url = "http://*.abc.com/*";
        EndpointBindings.getInstance().addRule(url, "camunda");
        String id = EndpointBindings.getInstance().getBindingIdentifierForEndpoint("aaa");
        Assertions.assertEquals("json-rpc", id);
        id = EndpointBindings.getInstance().getBindingIdentifierForEndpoint("http://gg.abc.com/home?a=b&k=f+2");
        Assertions.assertEquals("camunda", id);
    }

    @Test
    void getDefaultBindingIdentifier() {
        String url = "http://test.com";
        String id = EndpointBindings.getInstance().getBindingIdentifierForEndpoint(url);
        Assertions.assertEquals("json-rpc", id);
    }

    @Test
    void preprocess() {
        String url = "http://hotmail.com/*/ghareeb?aaa=x+y";
        String processed = "http://hotmail\\.com/\\S*/ghareeb\\?aaa=x\\+y";

        Assertions.assertEquals(processed, EndpointBindings.getInstance().preprocess(url));
    }
}