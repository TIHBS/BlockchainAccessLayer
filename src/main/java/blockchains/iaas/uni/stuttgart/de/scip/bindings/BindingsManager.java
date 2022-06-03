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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.CamundaBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.jsonrpc.JsonRpcBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the registered SCIP bindings
 */
public class BindingsManager {
    private static BindingsManager instance = null;
    private static final Logger log = LoggerFactory.getLogger(BindingsManager.class);
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
