/*******************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.management.callback;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.jsonrpc.model.ScipResponse;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CamundaMessage;
import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import com.github.arteam.simplejsonrpc.client.Transport;
import com.github.arteam.simplejsonrpc.client.builder.NotificationRequestBuilder;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class CallbackManager {
    private static CallbackManager instance = null;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private CallbackManager() {

    }

    public static CallbackManager getInstance() {
        if (instance == null)
            instance = new CallbackManager();

        return instance;
    }

    public void sendCallback(final String endpointUrl, final CallbackMessage responseBody) {
        log.info("Sending callback message {} to ({})", responseBody, endpointUrl);
        if (responseBody instanceof CamundaMessage) {
            this.sendRestCallback(endpointUrl, responseBody);
        } else if (responseBody instanceof ScipResponse) {
            this.sendJsonRpcCallback(endpointUrl, (ScipResponse) responseBody);
        } else {
            log.error("The sepcified response message has an unknown callback protocol.");
        }
    }

    private void sendRestCallback(final String endpointUrl, final CallbackMessage responseBody) {
        ResponseEntity<String> response = RestClient.create()
                .post()
                .uri(endpointUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(responseBody)
                .retrieve()
                .toEntity(String.class);

        log.info("Callback client responded with {} (code: {})",
                () -> response.getBody(), () -> response.getStatusCode());
    }

    private void sendJsonRpcCallback(final String endpointUrl, final ScipResponse response) {
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

        NotificationRequestBuilder builder = client.createNotification()
                .method(METHOD_NAME)
                .param("correlationIdentifier", response.getCorrelationIdentifier());

        if (response.getException() == null) {
            if (response.getParameters() != null) {
                builder = builder.param("parameters", response.getParameters());
            }

            if (response.getIsoTimestamp() != null) {
                builder = builder.param("timestamp", response.getIsoTimestamp());
            }

            if (response.getOccurrences() != null) {
                builder = builder.param("occurrences", response.getOccurrences());
            }
        } else {
            builder = builder.param("errorCode", response.getException().getCode());
            builder = builder.param("errorMessage", response.getException().getMessage());

            if (response.getException() instanceof TimeoutException) {
                builder = builder.param("transactionHash", ((TimeoutException) response.getException()).getTransactionHash());
                builder = builder.param("reachedDoC", ((TimeoutException) response.getException()).getDoc());
            }
        }

        builder.execute();
    }

    public void sendCallbackAsync(final String endpointUrl, final CallbackMessage responseBody) {
        this.executorService.execute(() -> sendCallback(endpointUrl, responseBody));
    }
}
