/*******************************************************************************
 * Copyright (c) 2019-2023 Institute for the Architecture of Application System - University of Stuttgart
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
import java.util.UUID;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.InvalidScipParameterException;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.api.model.QueryResult;
import blockchains.iaas.uni.stuttgart.de.api.model.TimeFrame;
import blockchains.iaas.uni.stuttgart.de.management.tccsci.DistributedTransactionManager;
import blockchains.iaas.uni.stuttgart.de.management.tccsci.DistributedTransactionRepository;
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
    private static final String DTX_ID_FIELD_NAME = "dtx_id";

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
        if (inputs.stream().anyMatch(p -> p.getName().equals(DTX_ID_FIELD_NAME))) {
            DistributedTransactionManager distributedTransactionManager = new DistributedTransactionManager();
            distributedTransactionManager.invokeSc(blockchainId, smartContractPath, functionIdentifier, inputs, outputs,
                    requiredConfidence, callbackUrl, timeoutMillis, correlationId, signature);
        } else {
            BlockchainManager manager = new BlockchainManager();
            manager.invokeSmartContractFunction(blockchainId, smartContractPath, functionIdentifier, inputs, outputs,
                    requiredConfidence, callbackUrl, timeoutMillis, correlationId, signature);
        }

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
    public String Unsubscribe(@JsonRpcOptional @JsonRpcParam("functionIdentifier") String functionIdentifier,
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

    @JsonRpcMethod
    public QueryResult Query(
            @JsonRpcOptional @JsonRpcParam("functionIdentifier") String functionIdentifier,
            @JsonRpcOptional @JsonRpcParam("eventIdentifier") String eventIdentifier,
            @JsonRpcOptional @JsonRpcParam("filter") String filter,
            @JsonRpcOptional @JsonRpcParam("timeframe") TimeFrame timeFrame,
            @JsonRpcParam("parameters") List<Parameter> outputParameters) {
        log.info("Query method is executed!");

        if (!Strings.isNullOrEmpty(functionIdentifier) && !Strings.isNullOrEmpty(eventIdentifier)) {
            throw new InvalidScipParameterException();
        }

        BlockchainManager manager = new BlockchainManager();

        if (!Strings.isNullOrEmpty(eventIdentifier)) {
            return manager.queryEvents(blockchainId, smartContractPath, eventIdentifier, outputParameters, filter, timeFrame);
        }

        throw new InvalidScipParameterException();
    }

    @JsonRpcMethod
    public String Start_Dtx() {
        log.info("Start_Dtx method is executed!");
        DistributedTransactionManager manager = new DistributedTransactionManager();

        return manager.startDtx().toString();
    }

    @JsonRpcMethod
    public String Commit_Dtx(@JsonRpcParam(DTX_ID_FIELD_NAME) String dtxId) {
        log.info("Commit_Dtx method is executed!");
        UUID uuid = UUID.fromString(dtxId);
        DistributedTransactionManager manager = new DistributedTransactionManager();
        manager.commitDtx(uuid);

        return "OK";
    }

    @JsonRpcMethod
    public String Abort_Dtx(@JsonRpcParam(DTX_ID_FIELD_NAME) String dtxId) {
        log.info("Abort_Dtx method is executed!");
        UUID uuid = UUID.fromString(dtxId);
        DistributedTransactionManager manager = new DistributedTransactionManager();
        manager.abortDtx(uuid);

        return "OK";
    }
}
