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

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import blockchains.iaas.uni.stuttgart.de.externalapi.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.externalapi.bindings.camunda.model.Message;
import blockchains.iaas.uni.stuttgart.de.externalapi.bindings.camunda.model.Variable;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.InvocationResponse;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.SubscriptionResponse;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamundaBinding implements AbstractBinding {
    private static final Logger log = LoggerFactory.getLogger(CamundaBinding.class);

    @Override
    public String getBindingIdentifier() {
        return "camunda";
    }

    @Override
    public void sendInvocationResponse(String endpointUrl, InvocationResponse response) {
        final Map<String, Variable> variables = new HashMap<>();
        response.getParams().forEach(parameter -> {
            Variable current = Variable
                    .builder()
                    .value(parameter.getValue())
                    .type("String")
                    .build();
            variables.put(parameter.getName(), current);
        });

        final Message message = Message
                .builder()
                .messageName("result_INOKE_" + response.getCorrelationIdentifier())
                .processInstanceId(response.getCorrelationIdentifier())
                .processVariables(variables)
                .build();

        final Client client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .build();
        final Entity entity = Entity.entity(message, MediaType.APPLICATION_JSON);
        final Response postResponse = client.target(endpointUrl).request(MediaType.APPLICATION_JSON)
                .post(entity);

        log.info("Callback client responded with code({})", postResponse.getStatus());
    }

    @Override
    public void sendSubscriptionResponse(String endpointUrl, SubscriptionResponse response) {

    }

    @Override
    public void sendErrorResponse(String endpointUrl, BalException exception) {

    }
}
