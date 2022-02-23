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

package blockchains.iaas.uni.stuttgart.de.restapi.callback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import blockchains.iaas.uni.stuttgart.de.adaptation.BlockchainAdapterFactory;
import blockchains.iaas.uni.stuttgart.de.config.ObjectMapperProvider;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CamundaMessage;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestCallbackManager {
    private static RestCallbackManager instance = null;
    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);
    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    private RestCallbackManager() {

    }

    public static RestCallbackManager getInstance() {
        if (instance == null)
            instance = new RestCallbackManager();

        return instance;
    }

    public void sendCallback(final String endpointUrl, final CallbackMessage responseBody) {
        if (responseBody instanceof CamundaMessage) {
            this.sendRestCallback(endpointUrl, responseBody);
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

    public void sendCallbackAsync(final String endpointUrl, final CallbackMessage responseBody) {
        this.executorService.execute(() -> sendCallback(endpointUrl, responseBody));
    }
}
