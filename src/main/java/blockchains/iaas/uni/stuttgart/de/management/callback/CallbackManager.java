/*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.management.callback;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import blockchains.iaas.uni.stuttgart.de.adaptation.BlockchainAdapterFactory;
import blockchains.iaas.uni.stuttgart.de.config.ObjectMapperProvider;
import blockchains.iaas.uni.stuttgart.de.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.jsonrpc.model.ScipResponse;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CamundaMessage;
import com.github.arteam.simplejsonrpc.client.JsonRpcClient;
import com.github.arteam.simplejsonrpc.client.Transport;
import com.github.arteam.simplejsonrpc.client.builder.NotificationRequestBuilder;
import com.google.common.base.Charsets;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallbackManager {
    private static CallbackManager instance = null;
    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private CallbackManager() {

    }

    public static CallbackManager getInstance() {
        if (instance == null)
            instance = new CallbackManager();

        return instance;
    }

    public void sendCallback(final String endpointUrl, final CallbackMessage responseBody) {
        if (responseBody instanceof CamundaMessage) {
            this.sendRestCallback(endpointUrl, responseBody);
        } else if (responseBody instanceof ScipResponse) {
            this.sendJsonRpcCallback(endpointUrl, (ScipResponse) responseBody);
        } else {
            log.error("The sepcified response message has an unknown callback protocol.");
        }
    }

    private void sendRestCallback(final String endpointUrl, final CallbackMessage responseBody) {
        final Client client = ClientBuilder.newBuilder()
                .register(ObjectMapperProvider.class)  // No need to register this provider if no special configuration is required.
                .register(JacksonFeature.class)
                .build();
        final Entity entity = Entity.entity(responseBody, MediaType.APPLICATION_JSON);
        final Response response = client.target(endpointUrl).request(MediaType.APPLICATION_JSON)
                .post(entity);

        log.info("Callback client responded with code({})", response.getStatus());
    }

    private void sendJsonRpcCallback(final String endpointUrl, final ScipResponse response) {
        final String METHOD_NAME = "ReceiveResponse";
        JsonRpcClient client = new JsonRpcClient(new Transport() {
            CloseableHttpClient httpClient = HttpClients.createDefault();

            @NotNull
            @Override
            public String pass(@NotNull String request) throws IOException {
                // Used Apache HttpClient 4.3.1 as an example
                HttpPost post = new HttpPost(endpointUrl);
                post.setEntity(new StringEntity(request, Charsets.UTF_8));
                post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                try (CloseableHttpResponse httpResponse = httpClient.execute(post)) {
                    return EntityUtils.toString(httpResponse.getEntity(), Charsets.UTF_8);
                }
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
