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

package blockchains.iaas.uni.stuttgart.de.externalapi.bindings.jsonrpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonRpcBindingTest {

    @Test
    void getBindingIdentifier() {
        JsonRpcBinding binding = new JsonRpcBinding();
        Assertions.assertEquals("json-rpc", binding.getBindingIdentifier());
    }

    @ParameterizedTest
    @
    void sendInvocationResponse() {
        JsonRpcBinding binding = new JsonRpcBinding();

    }

    @Test
    void sendSubscriptionResponse() {
    }

    @Test
    void sendErrorResponse() {
    }
}