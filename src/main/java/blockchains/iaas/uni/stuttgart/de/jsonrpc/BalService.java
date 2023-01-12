/*******************************************************************************
 * Copyright (c) 2019-2023 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-author: Akshay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.jsonrpc;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.InvalidScipParameterException;
import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.api.model.QueryResult;
import blockchains.iaas.uni.stuttgart.de.api.model.TimeFrame;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.models.PendingTransaction;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@JsonRpcService
@RequiredArgsConstructor
public class BalService {
    private static final Logger log = LoggerFactory.getLogger(BalService.class);
    private final String blockchainType;
    private final String blockchainId;
    private final String smartContractPath;
    BlockchainManager manager = BlockchainManager.getInstance();

    @JsonRpcMethod
    public String Invoke(
            @JsonRpcParam("functionIdentifier") String functionIdentifier,
            @JsonRpcParam("typeArguments") List<String> typeArguments,
            @JsonRpcParam("inputs") List<Parameter> inputs,
            @JsonRpcParam("outputs") List<Parameter> outputs,
            @JsonRpcParam("doc") double requiredConfidence,
            @JsonRpcParam("callbackUrl") String callbackUrl,
            @JsonRpcParam("timeout") long timeoutMillis,
            @JsonRpcParam("correlationIdentifier") String correlationId,
            @JsonRpcParam("signature") String signature,
            @JsonRpcParam("proposer") String proposer,
            @JsonRpcParam("signers") List<String> signers,
            @JsonRpcParam("minimumNumberOfSignatures") long minimumNumberOfSignatures

    ) {

        log.info("Invoke method is executed!");
        manager.createPendingInvocation(blockchainId, smartContractPath, functionIdentifier, typeArguments, inputs, outputs,
                requiredConfidence, callbackUrl, timeoutMillis, correlationId, signature, proposer, signers, minimumNumberOfSignatures);

        if (signers.size() == 0) {
            manager.invokeSmartContractFunction(blockchainId, smartContractPath, functionIdentifier, typeArguments, inputs, outputs,
                    requiredConfidence, callbackUrl, timeoutMillis, correlationId, signature, proposer, signers, minimumNumberOfSignatures);
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
        // BlockchainManager manager = new BlockchainManager();

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
                              @JsonRpcOptional @JsonRpcParam("typeArguments") List<String> typeArguments,
                              @JsonRpcParam("parameters") List<Parameter> parameters,
                              @JsonRpcParam("correlationIdentifier") String correlationId) {
        if (!Strings.isNullOrEmpty(functionIdentifier) && !Strings.isNullOrEmpty(eventIdentifier)) {
            throw new InvalidScipParameterException();
        }

        if (Strings.isNullOrEmpty(functionIdentifier) && Strings.isNullOrEmpty(eventIdentifier) && parameters != null) {
            throw new InvalidScipParameterException();
        }

        // BlockchainManager manager = new BlockchainManager();

        if (!Strings.isNullOrEmpty(functionIdentifier)) {
            manager.cancelFunctionSubscriptions(blockchainId, smartContractPath, correlationId, functionIdentifier, parameters, typeArguments);
        } else {
            manager.cancelEventSubscriptions(blockchainId, smartContractPath, correlationId, eventIdentifier, parameters);
        }

        return "OK";
    }

    @JsonRpcMethod
    public QueryResult Query(
            @JsonRpcOptional @JsonRpcParam("functionIdentifier") String functionIdentifier,
            @JsonRpcParam("typeArguments") List<String> typeArguments,
            @JsonRpcOptional @JsonRpcParam("eventIdentifier") String eventIdentifier,
            @JsonRpcOptional @JsonRpcParam("filter") String filter,
            @JsonRpcOptional @JsonRpcParam("timeframe") TimeFrame timeFrame,
            @JsonRpcParam("parameters") List<Parameter> outputParameters) {
        log.info("Query method is executed!");

        if (!Strings.isNullOrEmpty(functionIdentifier) && !Strings.isNullOrEmpty(eventIdentifier)) {
            throw new InvalidScipParameterException();
        }

        if (!Strings.isNullOrEmpty(eventIdentifier)) {
            return manager.queryEvents(blockchainId, smartContractPath, typeArguments, eventIdentifier, outputParameters, filter, timeFrame);
        }

        throw new InvalidScipParameterException();
    }

    @JsonRpcMethod
    public boolean Sign(
            @JsonRpcParam("signature") String signature,
            @JsonRpcParam("correlationIdentifier") String correlationId,
            @JsonRpcParam("signer") String signer
    ) {
        log.info("Sign method is executed!");
        return manager.signInvocation(correlationId, signature, signer);
    }

    @JsonRpcMethod
    public boolean Cancel(
            @JsonRpcParam("signature") String signature,
            @JsonRpcParam("correlationIdentifier") String correlationId,
            @JsonRpcParam("signer") String signer
    ) {
        log.info("Cancel method is executed!");
        return manager.tryCancelInvocation(correlationId, signature, signer);

    }

    @JsonRpcMethod
    public boolean Replace(
            @JsonRpcParam("functionIdentifier") String functionIdentifier,
            @JsonRpcParam("typeArguments") List<String> typeArguments,
            @JsonRpcParam("inputs") List<Parameter> inputs,
            @JsonRpcParam("outputs") List<Parameter> outputs,
            @JsonRpcParam("doc") double requiredConfidence,
            @JsonRpcParam("callbackUrl") String callbackUrl,
            @JsonRpcParam("timeout") long timeoutMillis,
            @JsonRpcParam("correlationIdentifier") String correlationId,
            @JsonRpcParam("signature") String signature,
            @JsonRpcParam("signers") List<String> signers,
            @JsonRpcParam("minimumNumberOfSignatures") long minimumNumberOfSignatures,
            @JsonRpcParam("signer") String signer
    ) {
        log.info("Replace method is executed!");
        return manager.tryReplaceInvocation(blockchainId, smartContractPath, functionIdentifier, typeArguments, inputs, outputs,
                requiredConfidence, callbackUrl, timeoutMillis, correlationId, signature, signer, signers, minimumNumberOfSignatures);

    }

    @JsonRpcMethod
    public List<PendingTransaction> Get() {
        log.info("Get method is executed!");

        return manager.getPendingInvocations();
    }
}
