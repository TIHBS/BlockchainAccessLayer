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

package blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import blockchains.iaas.uni.stuttgart.de.scip.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.Message;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.Variable;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvocationResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.Parameter;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscriptionResponse;
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
        final Map<String, Variable> variables = parseParameters(response.getParams());

        final Message message = Message
                .builder()
                .messageName("result_INOKE_" + response.getCorrelationIdentifier())
                .processInstanceId(response.getCorrelationIdentifier())
                .processVariables(variables)
                .build();

        sendCamundaMessage(message, endpointUrl);
    }

    @Override
    public void sendSubscriptionResponse(String endpointUrl, SubscriptionResponse response) {
        final Map<String, Variable> variables = parseParameters(response.getParams());

        final Message message = Message
                .builder()
                .messageName("result_SUBSCRIBE_" + response.getCorrelationIdentifier())
                .processInstanceId(response.getCorrelationIdentifier())
                .processVariables(variables)
                .build();

        sendCamundaMessage(message, endpointUrl);
    }

    @Override
    public void sendAsyncErrorResponse(String endpointUrl, AsynchronousBalException exception) {
        final Map<String, Variable> variables = new HashMap<>();

        if (exception.getCause() instanceof TimeoutException) {
            final Variable txHash = Variable
                    .builder()
                    .value(((TimeoutException) exception.getCause()).getTransactionHash())
                    .type("String")
                    .build();

            final Variable doc = Variable
                    .builder()
                    .value(String.valueOf(((TimeoutException) exception.getCause()).getDoc()))
                    .type("String")
                    .build();

            variables.put("reachedDoC", doc);
            variables.put("transactionHash", txHash);
        }

        Message message = Message
                .builder()
                .messageName("error_SUBSCRIBE_" + exception.getCorrelationIdentifier())
                .processInstanceId(exception.getCorrelationIdentifier())
                .processVariables(variables).build();

        sendCamundaMessage(message, endpointUrl);
    }

    private Map<String, Variable> parseParameters(List<Parameter> parameterList) {
        final Map<String, Variable> variables = new HashMap<>();
        if (parameterList != null) {
            parameterList.forEach(parameter -> {
                Variable current = Variable
                        .builder()
                        .value(parameter.getValue())
                        .type("String")
                        .build();
                variables.put(parameter.getName(), current);
            });
        }

        return variables;
    }

    private void sendCamundaMessage(Message message, String endpointUrl) {
        final Client client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .build();
        final Entity entity = Entity.entity(message, MediaType.APPLICATION_JSON);
        final Response postResponse = client.target(endpointUrl).request(MediaType.APPLICATION_JSON)
                .post(entity);

        log.info("Callback client responded with code({})", postResponse.getStatus());
    }
}
