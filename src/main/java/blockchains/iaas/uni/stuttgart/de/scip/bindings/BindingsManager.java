/*******************************************************************************
 * Copyright (c) 2024 Institute for the Architecture of Application System - University of Stuttgart
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
import java.util.HashMap;
import java.util.Map;

import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.CamundaBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.jsonrpc.JsonRpcBinding;
import lombok.extern.log4j.Log4j2;

@Log4j2
/**
 * Manages the registered SCIP bindings
 */
public class BindingsManager {
    private static BindingsManager instance = null;
    private final Map<String, AbstractBinding> bindings;

    private BindingsManager() {
        final CamundaBinding camunda = new CamundaBinding();
        final JsonRpcBinding jsonrpc = new JsonRpcBinding();
        bindings = new HashMap<>();
        bindings.put(camunda.getBindingIdentifier(), camunda);
        bindings.put(jsonrpc.getBindingIdentifier(), jsonrpc);
    }

    public static BindingsManager getInstance() {
        if (instance == null)
            instance = new BindingsManager();

        return instance;
    }

    /**
     * Retrieves the binding object that corresponds to the provided binding identifier
     * @param bindingIdentifier the identifier of the binding to be retrieved
     * @return the binding that corresponds to the provided identifier, or null if no binding is mapped to it.
     */
    public AbstractBinding getBinding(String bindingIdentifier) {
        return bindings.get(bindingIdentifier);
    }

    /**
     * Retrieves a collection of all the binding identifiers available.
     * @return a collection of all the binding identifiers available.
     */
    public Collection<String> getAvailableBindingIdentifiers() {
        return bindings.keySet();
    }
}