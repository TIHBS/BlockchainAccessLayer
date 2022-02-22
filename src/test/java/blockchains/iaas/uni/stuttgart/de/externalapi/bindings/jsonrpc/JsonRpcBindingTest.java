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

package blockchains.iaas.uni.stuttgart.de.externalapi.bindings.jsonrpc;

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blockchains.iaas.uni.stuttgart.de.externalapi.model.exceptions.InvalidScipParameterException;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.InvocationResponse;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.Parameter;
import blockchains.iaas.uni.stuttgart.de.externalapi.model.responses.SubscriptionResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JsonRpcBindingTest {
    private MockWebServer mockWebServer;
    private static final Logger log = LoggerFactory.getLogger(JsonRpcBindingTest.class);

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
        JsonRpcBinding binding = new JsonRpcBinding();
        Assertions.assertEquals("json-rpc", binding.getBindingIdentifier());
    }

    @ParameterizedTest
    @MethodSource
    void sendInvocationResponse(InvocationResponse response) throws InterruptedException {
        String endpointUrl = this.mockWebServer.url("/").toString();
        JsonRpcBinding binding = new JsonRpcBinding();
        binding.sendInvocationResponse(endpointUrl, response);
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        // todo test contents of request message
    }

    @ParameterizedTest
    @MethodSource
    void sendSubscriptionResponse(SubscriptionResponse response) throws InterruptedException {
        String endpointUrl = this.mockWebServer.url("/").toString();
        JsonRpcBinding binding = new JsonRpcBinding();
        binding.sendSubscriptionResponse(endpointUrl, response);
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        // todo test contents of request message
    }

    @Test
    void sendErrorResponses() throws InterruptedException {
        InvalidScipParameterException e1 = new InvalidScipParameterException("The first Exception occurred");
        TimeoutException e2 = new TimeoutException("The second exception occurred", "CRAZYHASH123", 0.3);
        String endpointUrl = this.mockWebServer.url("/").toString();
        JsonRpcBinding binding = new JsonRpcBinding();
        binding.sendErrorResponse(endpointUrl, e1);
        binding.sendErrorResponse(endpointUrl, e2);
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        // todo test contents of request message
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

    private static Stream<Arguments> sendSubscriptionResponse() {
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
        SubscriptionResponse response1 = SubscriptionResponse
                .builder()
                .correlationIdentifier("1234")
                .timestamp("654321")
                .params(Stream
                        .of(param1, param2)
                        .collect(Collectors.toList()))
                .build();
        // a missing corrId throws an exception
        // here the timestamp is missing
        SubscriptionResponse response3 = SubscriptionResponse
                .builder()
                .correlationIdentifier("1234")
                .params(Stream
                        .of(param1, param2)
                        .collect(Collectors.toList()))
                .build();
        // here the parameters are missing
        SubscriptionResponse response4 = SubscriptionResponse
                .builder()
                .correlationIdentifier("1234")
                .timestamp("654321")
                .build();
        // here the parameters are provided as an empty collection
        SubscriptionResponse response5 = SubscriptionResponse
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