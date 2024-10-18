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

package blockchains.iaas.uni.stuttgart.de.scip.bindings.jsonrpc;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.AbstractBinding;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvokeResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscribeResponse;
import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import com.github.arteam.simplejsonrpc.client.Transport;
import com.github.arteam.simplejsonrpc.client.builder.NotificationRequestBuilder;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Log4j2
public class JsonRpcBinding implements AbstractBinding {

    @Override
    public String getBindingIdentifier() {
        return "json-rpc";
    }

    @Override
    public void sendInvocationResponse(String endpointUrl, InvokeResponse response) {
        try {
            NotificationRequestBuilder builder = createNotificationBuilder(endpointUrl);

            builder.param("correlationIdentifier", response.getCorrelationId())
                    .param("parameters", response.getOutputArguments())
                    .param("timestamp", response.getTimeStamp() == null ? "" : response.getTimeStamp())
                    .execute();
        } catch (Exception e) {
            log.error("Failed to send Invocation response to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    @Override
    public void sendSubscriptionResponse(String endpointUrl, SubscribeResponse response) {
        try {
            NotificationRequestBuilder builder = createNotificationBuilder(endpointUrl);
            builder.param("correlationIdentifier", response.getCorrelationId())
                    .param("parameters", response.getArguments())
                    .param("timestamp", response.getTimestamp())
                    .execute();
        } catch (Exception e) {
            log.error("Failed to send Subscription response to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    @Override
    public void sendAsyncErrorResponse(String endpointUrl, AsynchronousBalException exception) {
        try {
            NotificationRequestBuilder builder = createNotificationBuilder(endpointUrl);

            builder = builder.param("errorCode", exception.getCode());
            builder = builder.param("errorMessage", exception.getMessage());
            builder = builder.param("correlationIdentifier", exception.getCorrelationIdentifier());

            if (exception.getCause() instanceof TimeoutException) {
                builder = builder.param("transactionHash",
                        ((TimeoutException) exception.getCause()).getTransactionHash());
                builder = builder.param("reachedDoC",
                        ((TimeoutException) exception.getCause()).getDoc());
            }

            builder.execute();
        } catch (Exception e) {
            log.error("Failed to send asynchronous error to {}. Reason: {}", endpointUrl, e.getMessage());
        }
    }

    private NotificationRequestBuilder createNotificationBuilder(final String endpointUrl) {
        final String METHOD_NAME = "ReceiveResponse";
        JsonRpcClient client = new JsonRpcClient(new Transport() {
            @NotNull
            @Override
            public String pass(@NotNull String request) throws IOException {
                ResponseEntity<String> response = RestClient.create()
                        .post()
                        .uri(endpointUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .retrieve()
                        .toEntity(String.class);

                log.info("Callback client responded with {} (code: {})",
                        () -> response.getBody(), () -> response.getStatusCode());

                return response.getBody() != null ? response.getBody() : "";
            }

        });

        return client.createNotification()
                .method(METHOD_NAME);

    }
}