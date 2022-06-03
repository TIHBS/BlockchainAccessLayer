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

package blockchains.iaas.uni.stuttgart.de.scip.callback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import blockchains.iaas.uni.stuttgart.de.adaptation.BlockchainAdapterFactory;
import blockchains.iaas.uni.stuttgart.de.restapi.callback.RestCallbackManager;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.EndpointBindings;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvocationResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscriptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScipCallbackManager {
    private static ScipCallbackManager instance = null;
    private static final Logger log = LoggerFactory.getLogger(ScipCallbackManager.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private ScipCallbackManager() {

    }

    public static ScipCallbackManager getInstance() {
        if (instance == null)
            instance = new ScipCallbackManager();

        return instance;
    }

    public void sendInvocationResponse(String endpointUrl, InvocationResponse response) {
        AbstractBinding binding = EndpointBindings.getInstance().getBindingForEndpoint(endpointUrl);
        this.executorService.execute(() -> binding.sendInvocationResponse(endpointUrl, response));
    }

    public void sendSubscriptionResponse(String endpointUrl, SubscriptionResponse response) {
        AbstractBinding binding = EndpointBindings.getInstance().getBindingForEndpoint(endpointUrl);
        this.executorService.execute(() -> binding.sendSubscriptionResponse(endpointUrl, response));
    }

    public void sendAsyncErrorResponse(String endpointUrl, AsynchronousBalException exception) {
        AbstractBinding binding = EndpointBindings.getInstance().getBindingForEndpoint(endpointUrl);
        this.executorService.execute(() -> binding.sendAsyncErrorResponse(endpointUrl, exception));
    }
}
