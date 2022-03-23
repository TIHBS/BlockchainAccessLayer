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

import blockchains.iaas.uni.stuttgart.de.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.DoubleVariable;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.LongVariable;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.Message;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.StringVariable;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.Variable;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvocationResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.Parameter;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscriptionResponse;
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
        try {
            sendResultResponse(response.getParams(),
                    response.getTimestamp(),
                    response.getCorrelationIdentifier(),
                    endpointUrl);
        } catch (Exception e) {
            log.error("Failed to send Invocation response to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    @Override
    public void sendSubscriptionResponse(String endpointUrl, SubscriptionResponse response) {
        try {
            sendResultResponse(response.getParams(),
                    response.getTimestamp(),
                    response.getCorrelationIdentifier(),
                    endpointUrl);
        } catch (Exception e) {
            log.error("Failed to send Subscription response to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    @Override
    public void sendAsyncErrorResponse(String endpointUrl, AsynchronousBalException exception) {
        try {
            final Map<String, Variable> variables = new HashMap<>();

            if (exception.getCause() instanceof TimeoutException) {
                final Variable txHash = new StringVariable(((TimeoutException) exception.getCause()).getTransactionHash());
                final Variable doc = new DoubleVariable(((TimeoutException) exception.getCause()).getDoc());
                variables.put("reachedDoC", doc);
                variables.put("transactionHash", txHash);
            }

            final Variable errCode = new LongVariable(exception.getCode());
            final Variable errMessage = new StringVariable(exception.getMessage());
            final String messageName = "error_" + exception.getCorrelationIdentifier();
            variables.put("errorCode", errCode);
            variables.put("errorMessage", errMessage);
            Message message = Message
                    .builder()
                    .messageName(messageName)
                    .processInstanceId(exception.getCorrelationIdentifier())
                    .processVariables(variables).build();

            sendCamundaMessage(message, endpointUrl);
        } catch (Exception e) {
            log.error("Failed to send asynchronous error to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    private Map<String, Variable> parseParameters(List<Parameter> parameterList) {
        final Map<String, Variable> variables = new HashMap<>();
        if (parameterList != null) {
            parameterList.forEach(parameter -> {
                Variable current = new StringVariable(parameter.getValue());
                variables.put(parameter.getName(), current);
            });
        }

        return variables;
    }

    private void sendResultResponse(List<Parameter> parameters, String timestamp, String correlationIdentifier,
                                    String endpointUrl) {
        final Map<String, Variable> variables = parseParameters(parameters);
        long timestampL = timestamp == null || timestamp.equals("") ?
                0 : Long.parseLong(timestamp);
        variables.put("timestamp", new LongVariable(timestampL));

        final Message message = Message
                .builder()
                .messageName("result_" + correlationIdentifier)
                .processInstanceId(correlationIdentifier)
                .processVariables(variables)
                .build();

        sendCamundaMessage(message, endpointUrl);
    }

    private void sendCamundaMessage(Message message, String endpointUrl) {
        final Client client = ClientBuilder.newBuilder()
                //.register(JacksonFeature.class)
                .build();
        final Entity entity = Entity.entity(message, MediaType.APPLICATION_JSON);
        final Response postResponse = client.target(endpointUrl).request(MediaType.APPLICATION_JSON)
                .post(entity);

        log.info("Callback client responded with code({})", postResponse.getStatus());
    }
}
