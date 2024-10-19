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


import blockchains.iaas.uni.stuttgart.de.api.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.DoubleVariable;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.LongVariable;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.Message;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.StringVariable;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model.Variable;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;

import blockchains.iaas.uni.stuttgart.de.scip.model.common.Argument;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@Log4j2
public class CamundaBinding implements AbstractBinding {

    @Override
    public String getBindingIdentifier() {
        return "camunda";
    }

    @Override
    public void sendAsyncResponse(String endpointUrl, AsyncScipResponse response) {
        if (response instanceof InvokeResponse invokeResponse) {
            this.sendInvocationResponse(endpointUrl, invokeResponse);
        } else if (response instanceof SubscribeResponse subscribeResponse) {
            this.sendSubscriptionResponse(endpointUrl, subscribeResponse);
        } else if (response instanceof SendTxResponse sendTxResponse) {
            this.sendSendTxResponse(endpointUrl, sendTxResponse);
        } else if (response instanceof ReceiveTxResponse receiveTxResponse) {
            this.sendReceiveTxResponse(endpointUrl, receiveTxResponse);
        } else if (response instanceof EnsureStateResponse ensureStateResponse) {
            this.sendEnsureStateResponse(endpointUrl, ensureStateResponse);
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
                    .processVariables(variables).build();

            sendCamundaMessage(message, endpointUrl);
        } catch (Exception e) {
            log.error("Failed to send asynchronous error to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    protected void sendInvocationResponse(String endpointUrl, InvokeResponse response) {
        try {
            sendResultResponse(response.getOutputArguments(),
                    response.getTimeStamp(),
                    response.getCorrelationId(),
                    endpointUrl);
        } catch (Exception e) {
            log.error("Failed to send InvokeResponse to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }


    protected void sendSubscriptionResponse(String endpointUrl, SubscribeResponse response) {
        try {
            sendResultResponse(response.getArguments(),
                    response.getTimestamp(),
                    response.getCorrelationId(),
                    endpointUrl);
        } catch (Exception e) {
            log.error("Failed to send SubscribeResponse to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    protected void sendSendTxResponse(String endpointUrl, SendTxResponse response) {
        try {
            sendResultResponse(null,
                    response.getTimestamp(),
                    response.getCorrelationId(),
                    endpointUrl);
        } catch (Exception e) {
            log.error("Failed to send SendTxResponse to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    protected void sendEnsureStateResponse(String endpointUrl, EnsureStateResponse response) {
        try {
            sendResultResponse(null,
                    null,
                    response.getCorrelationId(),
                    endpointUrl);
        } catch (Exception e) {
            log.error("Failed to send EnsureStateResponse to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    protected void sendReceiveTxResponse(String endpointUrl, ReceiveTxResponse response) {
        try {
            final Map<String, Variable> variables = new HashMap<>();
            variables.put("from", new StringVariable(response.getFrom()));
            variables.put("value", new LongVariable(response.getValue()));

            if (!(response.getTimestamp() == null || response.getTimestamp().isEmpty())) {
                variables.put("timestamp", new LongVariable(Long.parseLong(response.getTimestamp())));
            }

            sendCamundaMessage(response.getCorrelationId(), endpointUrl, variables);
        } catch (Exception e) {
            log.error("Failed to send ReceiveTxResponse to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    private void sendResultResponse(List<Argument> arguments, String timestamp, String correlationIdentifier,
                                    String endpointUrl) {
        final Map<String, Variable> variables = parseArguments(arguments);

        if (!(timestamp == null || timestamp.isEmpty())) {
            variables.put("timestamp", new LongVariable(Long.parseLong(timestamp)));
        }

        sendCamundaMessage(correlationIdentifier, endpointUrl, variables);
    }

    private void sendCamundaMessage(String correlationId, String endpointUrl, final Map<String, Variable> variables) {
        final Message message = Message
                .builder()
                .messageName("result_" + correlationId)
                .processVariables(variables)
                .build();

        sendCamundaMessage(message, endpointUrl);
    }

    private Map<String, Variable> parseArguments(List<Argument> parameterList) {
        final Map<String, Variable> variables = new HashMap<>();
        if (parameterList != null) {
            parameterList.forEach(parameter -> {
                Variable current = new StringVariable(parameter.getValue());
                variables.put(parameter.getName(), current);
            });
        }

        return variables;
    }

    private void sendCamundaMessage(Message message, String endpointUrl) {

        ResponseEntity<String> response = RestClient.create()
                .post()
                .uri(endpointUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(message)
                .retrieve()
                .toEntity(String.class);

        log.info("Callback client responded with {} (code: {})",
                () -> response.getBody(), () -> response.getStatusCode());
    }
}