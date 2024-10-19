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

package blockchains.iaas.uni.stuttgart.de.restapi.callback;


import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CallbackMessage;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.CamundaMessage;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class RestCallbackManager {
    private static RestCallbackManager instance = null;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    private RestCallbackManager() {

    }

    public static RestCallbackManager getInstance() {
        if (instance == null)
            instance = new RestCallbackManager();

        return instance;
    }

    public void sendCallback(final String endpointUrl, final CallbackMessage responseBody) {
        log.info("Sending REST callback message {} to ({})", responseBody, endpointUrl);
        if (responseBody instanceof CamundaMessage) {
            this.sendRestCallback(endpointUrl, responseBody);
        } else {
            log.error("The specified response message has an unknown callback protocol.");
        }
    }

    public void sendCallbackAsync(final String endpointUrl, final CallbackMessage responseBody) {
        this.executorService.execute(() -> sendCallback(endpointUrl, responseBody));
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


}
