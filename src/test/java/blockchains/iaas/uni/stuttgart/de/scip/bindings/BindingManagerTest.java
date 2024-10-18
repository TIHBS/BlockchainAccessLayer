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

import java.util.Collection;

import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.CamundaBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.jsonrpc.JsonRpcBinding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BindingsManagerTest {

    @Test
    void getBinding() {
        AbstractBinding binding = BindingsManager.getInstance().getBinding("camunda");
        assertInstanceOf(CamundaBinding.class, binding);
        binding = BindingsManager.getInstance().getBinding("json-rpc");
        assertInstanceOf(JsonRpcBinding.class, binding);
    }

    @Test
    void getAvailableBindingIdentifiers() {
        Collection<String> bindingIds = BindingsManager.getInstance().getAvailableBindingIdentifiers();
        Assertions.assertTrue(bindingIds.contains("camunda"));
        Assertions.assertTrue(bindingIds.contains("json-rpc"));
    }
}