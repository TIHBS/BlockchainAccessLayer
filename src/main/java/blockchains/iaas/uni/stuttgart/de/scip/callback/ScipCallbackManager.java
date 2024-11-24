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

import blockchains.iaas.uni.stuttgart.de.scip.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.BindingsManager;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.AsyncScipResponse;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class ScipCallbackManager {
    private static ScipCallbackManager instance = null;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final int FORCED_SLEEP_MILLIS = 10 * 1000;

    private ScipCallbackManager() {

    }

    public static ScipCallbackManager getInstance() {
        if (instance == null)
            instance = new ScipCallbackManager();

        return instance;
    }

    public void sendAsyncResponse(String endpointUrl, String bindingName, AsyncScipResponse response) {
        log.info("Sending SCIP {} to {} using the binding '{}' (CorrelationId={}).\nResponse body: {}",
                () -> response.getClass().getSimpleName(),
                () -> endpointUrl,
                () -> bindingName,
                () -> response.getCorrelationId(),
                () -> response);
        try {
            log.debug("Waiting {} millis before sending callback", FORCED_SLEEP_MILLIS);
            Thread.sleep(FORCED_SLEEP_MILLIS);
            AbstractBinding binding = BindingsManager.getInstance().getBinding(bindingName);
            this.executorService.execute(() -> binding.sendAsyncResponse(endpointUrl, response));
        } catch (InterruptedException e) {
            log.error(e);
        }

    }


    public void sendAsyncErrorResponse(String endpointUrl, String bindingName, AsynchronousBalException exception) {
        log.info("Sending asynchronous SCIP error to {} using the binding '{}'.\nException body: {}", endpointUrl, bindingName, exception);
        try {
            log.debug("Waiting {} millis before sending callback", FORCED_SLEEP_MILLIS);
            Thread.sleep(FORCED_SLEEP_MILLIS);
            AbstractBinding binding = BindingsManager.getInstance().getBinding(bindingName);
            this.executorService.execute(() -> binding.sendAsyncErrorResponse(endpointUrl, exception));
        } catch (InterruptedException e) {
            log.error(e);
        }

    }
}