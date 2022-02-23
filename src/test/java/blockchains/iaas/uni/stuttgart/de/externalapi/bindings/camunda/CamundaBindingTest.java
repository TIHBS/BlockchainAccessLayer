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

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.InvocationResponse;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.Parameter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CamundaBindingTest {
    private MockWebServer mockWebServer;
    private static final Logger log = LoggerFactory.getLogger(CamundaBindingTest.class);

    @BeforeEach
    void init() {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(200));
    }

    @AfterEach
    void destroy() throws IOException {
        this.mockWebServer.close();
    }

    @Test
    void getBindingIdentifier() {
    }

    @ParameterizedTest
    @MethodSource
    void sendInvocationResponse(InvocationResponse response) throws InterruptedException {
        String endpointUrl = this.mockWebServer.url("/").toString();
        CamundaBinding binding = new CamundaBinding();
        binding.sendInvocationResponse(endpointUrl, response);
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        // todo test contents of request message
    }

    @Test
    void sendSubscriptionResponse() {
    }

    @Test
    void sendErrorResponse() {
    }

    private static Stream<Arguments> sendInvocationResponse() {
        Parameter param1 = Parameter
                .builder()
                .name("param1")
                .value("Great Test!")
                .build();
        Parameter param2 = Parameter
                .builder()
                .name("param2")
                .value("Super Test!")
                .build();
        // here everything is provided
        InvocationResponse response1 = InvocationResponse
                .builder()
                .correlationIdentifier("1234")
                .timestamp("654321")
                .params(Stream
                        .of(param1, param2)
                        .collect(Collectors.toList()))
                .build();
        // a missing corrId throws an exception
        // here the timestamp is missing
        InvocationResponse response3 = InvocationResponse
                .builder()
                .correlationIdentifier("1234")
                .params(Stream
                        .of(param1, param2)
                        .collect(Collectors.toList()))
                .build();
        // here the parameters are missing
        InvocationResponse response4 = InvocationResponse
                .builder()
                .correlationIdentifier("1234")
                .timestamp("654321")
                .build();
        // here the parameters are provided as an empty collection
        InvocationResponse response5 = InvocationResponse
                .builder()
                .correlationIdentifier("1234")
                .timestamp("654321")
                .params(Collections.emptyList())
                .build();

        return Stream.of(
                Arguments.of(response1),
                Arguments.of(response3),
                Arguments.of(response4),
                Arguments.of(response5)
        );
    }
}