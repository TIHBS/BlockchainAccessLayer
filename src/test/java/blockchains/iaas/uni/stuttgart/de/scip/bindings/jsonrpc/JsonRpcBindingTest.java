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

import java.io.IOException;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.InvokeSmartContractFunctionFailure;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.TimeoutException;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvokeResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.Argument;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscribeResponse;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
class JsonRpcBindingTest {
    private MockWebServer mockWebServer;

    @BeforeEach
    void init() {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.enqueue(new MockResponse().setResponseCode(200));
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
    void sendInvokeResponse(InvokeResponse response) throws InterruptedException {
        String endpointUrl = this.mockWebServer.url("/").toString();
        JsonRpcBinding binding = new JsonRpcBinding();
        binding.sendInvocationResponse(endpointUrl, response);
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        // todo test contents of request message
    }

    @ParameterizedTest
    @MethodSource
    void sendSubscribeResponse(SubscribeResponse response) throws InterruptedException {
        String endpointUrl = this.mockWebServer.url("/").toString();
        JsonRpcBinding binding = new JsonRpcBinding();
        binding.sendSubscriptionResponse(endpointUrl, response);
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        // todo test contents of request message
    }

    @Test
    void sendErrorResponses() throws InterruptedException {
        AsynchronousBalException e1 = new AsynchronousBalException(
                new InvokeSmartContractFunctionFailure("The first Exception occurred"),
                "654321ABC");
        AsynchronousBalException e2 = new AsynchronousBalException(
                new TimeoutException("The first Exception occurred", "CRAZYHASH123", 0.3),
                "XXX654321ABC");
        String endpointUrl = this.mockWebServer.url("/").toString();
        JsonRpcBinding binding = new JsonRpcBinding();
        binding.sendAsyncErrorResponse(endpointUrl, e1);
        binding.sendAsyncErrorResponse(endpointUrl, e2);
        RecordedRequest recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        recordedRequest = this.mockWebServer.takeRequest();
        log.debug(recordedRequest.getBody().readUtf8());
        // todo test contents of request message
    }

    private static Stream<Arguments> sendInvokeResponse() {
        Argument param1 = Argument
                .builder()
                .name("param1")
                .value("Great Test!")
                .build();
        Argument param2 = Argument
                .builder()
                .name("param2")
                .value("Super Test!")
                .build();
        // here everything is provided
        InvokeResponse response1 = InvokeResponse
                .builder()
                .correlationId("1234")
                .timeStamp("654321")
                .outputArguments(Stream
                        .of(param1, param2)
                        .collect(Collectors.toList()))
                .build();

        // here the parameters are provided as an empty collection
        InvokeResponse response2 = InvokeResponse
                .builder()
                .correlationId("1234")
                .timeStamp("654321")
                .outputArguments(Collections.emptyList())
                .build();

        return Stream.of(
                Arguments.of(response1),
                Arguments.of(response2)
        );
    }

    private static Stream<Arguments> sendSubscribeResponse() {
        Argument param1 = Argument
                .builder()
                .name("param1")
                .value("Great Test!")
                .build();
        Argument param2 = Argument
                .builder()
                .name("param2")
                .value("Super Test!")
                .build();
        // here everything is provided
        SubscribeResponse response1 = SubscribeResponse
                .builder()
                .correlationId("1234")
                .timestamp("654321")
                .arguments(Stream
                        .of(param1, param2)
                        .collect(Collectors.toList()))
                .build();

        // here the parameters are provided as an empty collection
        SubscribeResponse response3 = SubscribeResponse
                .builder()
                .correlationId("1234")
                .timestamp("654321")
                .arguments(Collections.emptyList())
                .build();

        return Stream.of(
                Arguments.of(response1),
                Arguments.of(response3)
        );
    }
}