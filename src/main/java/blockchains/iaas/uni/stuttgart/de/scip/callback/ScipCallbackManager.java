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

package blockchains.iaas.uni.stuttgart.de.scip.callback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import blockchains.iaas.uni.stuttgart.de.scip.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.BindingsManager;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvokeResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscribeResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ScipCallbackManager {
    private static ScipCallbackManager instance = null;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private ScipCallbackManager() {

    }

    public static ScipCallbackManager getInstance() {
        if (instance == null)
            instance = new ScipCallbackManager();

        return instance;
    }

    public void sendInvocationResponse(String endpointUrl, String bindingName, InvokeResponse response) {
        log.info("Sending SCIP InvokeResponse to {} using binding {}. Response body: {}", endpointUrl, bindingName, response);
        AbstractBinding binding = BindingsManager.getInstance().getBinding(bindingName);
        this.executorService.execute(() -> binding.sendInvocationResponse(endpointUrl, response));
    }

    public void sendSubscriptionResponse(String endpointUrl, String bindingName, SubscribeResponse response) {
        log.info("Sending SCIP SubscribeResponse to {} using binding {}. Response body: {}", endpointUrl, bindingName, response);
        AbstractBinding binding = BindingsManager.getInstance().getBinding(bindingName);
        this.executorService.execute(() -> binding.sendSubscriptionResponse(endpointUrl, response));
    }

    public void sendAsyncErrorResponse(String endpointUrl, String bindingName, AsynchronousBalException exception) {
        log.info("Sending asynchronous SCIP error to {} using binding {}. Exception body: {}", endpointUrl, bindingName, exception);
        AbstractBinding binding = BindingsManager.getInstance().getBinding(bindingName);
        this.executorService.execute(() -> binding.sendAsyncErrorResponse(endpointUrl, exception));
    }
}