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

import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvokeResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscribeResponse;


public interface AbstractBinding {
    String getBindingIdentifier();

    void sendInvocationResponse(String endpointUrl, InvokeResponse response);

    void sendSubscriptionResponse(String endpointUrl, SubscribeResponse response);

    void sendAsyncErrorResponse(String endpointUrl, AsynchronousBalException exception);
}