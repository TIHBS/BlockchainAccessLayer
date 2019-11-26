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

package blockchains.iaas.uni.stuttgart.de.jsonrpc;

import java.util.List;

import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidScipParameterException;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonRpcService
@AllArgsConstructor
public class BalService {
    private static final Logger log = LoggerFactory.getLogger(BalService.class);
    private final String blockchainType;
    private final String blockchainId;
    private final String smartContractPath;

    @JsonRpcMethod
    public String Invoke(
            @JsonRpcParam("functionIdentifier") String functionIdentifier,
            @JsonRpcParam("inputs") List<Parameter> inputs,
            @JsonRpcParam("outputs") List<Parameter> outputs,
            @JsonRpcParam("doc") double requiredConfidence,
            @JsonRpcParam("callbackUrl") String callbackUrl,
            @JsonRpcParam("timeout") long timeoutMillis,
            @JsonRpcParam("correlationIdentifier") String correlationId,
            @JsonRpcParam("signature") String signature
    ) {
        log.info("Invoke method is executed!");
        BlockchainManager manager = new BlockchainManager();
        manager.invokeSmartContractFunction(blockchainId, smartContractPath, functionIdentifier, inputs, outputs,
                requiredConfidence, callbackUrl, timeoutMillis, correlationId, signature);

        return "OK";
    }

    @JsonRpcMethod
    public String Subscribe(
            @JsonRpcOptional @JsonRpcParam("functionIdentifier") String functionIdentifier,
            @JsonRpcOptional @JsonRpcParam("eventIdentifier") String eventIdentifier,
            @JsonRpcParam("parameters") List<Parameter> outputParameters,
            @JsonRpcParam("doc") double degreeOfConfidence,
            @JsonRpcParam("filter") String filter,
            @JsonRpcParam("callbackUrl") String callbackUrl,
            @JsonRpcParam("correlationIdentifier") String correlationId) {
        log.info("Subscribe method is executed!");
        BlockchainManager manager = new BlockchainManager();

        if (!Strings.isNullOrEmpty(functionIdentifier) && !Strings.isNullOrEmpty(eventIdentifier)) {
            throw new InvalidScipParameterException();
        }

        if (!Strings.isNullOrEmpty(eventIdentifier)) {
            manager.subscribeToEvent(blockchainId, smartContractPath, eventIdentifier, outputParameters, degreeOfConfidence, filter, callbackUrl, correlationId);
        }

        return "OK";
    }

    @JsonRpcMethod
    public String Cancel(@JsonRpcOptional @JsonRpcParam("functionIdentifier") String functionIdentifier,
                         @JsonRpcOptional @JsonRpcParam("eventIdentifier") String eventIdentifier,
                         @JsonRpcParam("parameters") List<Parameter> parameters,
                         @JsonRpcParam("correlationIdentifier") String correlationId) {
        if (!Strings.isNullOrEmpty(functionIdentifier) && !Strings.isNullOrEmpty(eventIdentifier)) {
            throw new InvalidScipParameterException();
        }

        if (Strings.isNullOrEmpty(functionIdentifier) && Strings.isNullOrEmpty(eventIdentifier) && parameters != null) {
            throw new InvalidScipParameterException();
        }

        BlockchainManager manager = new BlockchainManager();

        if (!Strings.isNullOrEmpty(functionIdentifier)) {
            manager.cancelFunctionSubscriptions(blockchainId, smartContractPath, correlationId, functionIdentifier, parameters);
        } else {
            manager.cancelEventSubscriptions(blockchainId, smartContractPath, correlationId, eventIdentifier, parameters);
        }

        return "OK";
    }
}
