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

package blockchains.iaas.uni.stuttgart.de.externalapi.bindings.camunda;

import blockchains.iaas.uni.stuttgart.de.externalapi.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.InvocationResponse;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.SubscriptionResponse;

public class CamundaBinding implements AbstractBinding {
    @Override
    public String getBindingIdentifier() {
        return "camunda";
    }

    @Override
    public void sendInvocationResponse(String endpointUrl, InvocationResponse response) {

    }

    @Override
    public void sendSubscriptionResponse(String endpointUrl, SubscriptionResponse response) {

    }

    @Override
    public void sendErrorResponse(String endpointUrl, BalException exception) {

    }
}
