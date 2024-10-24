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

package blockchains.iaas.uni.stuttgart.de.scip;

import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.InvalidScipParameterException;
import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.api.model.QueryResult;
import blockchains.iaas.uni.stuttgart.de.api.model.TimeFrame;
import blockchains.iaas.uni.stuttgart.de.scip.model.common.MemberSignature;
import blockchains.iaas.uni.stuttgart.de.scip.model.common.Argument;
import blockchains.iaas.uni.stuttgart.de.tccsci.DistributedTransactionManager;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcOptional;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.UUID;

@JsonRpcService
@Log4j2
public class ScipService {
    private static final String DTX_ID_FIELD_NAME = "dtx_id";
    private final String blockchainType;
    private final String blockchainId;
    private final String smartContractPath;
    private final BlockchainManager manager;
    private final DistributedTransactionManager dtxManager;

    public ScipService(String blockchainType, String blockchainId, String smartContractPath, BlockchainManager manager, DistributedTransactionManager dtxManager) {
        this.blockchainType = blockchainType;
        this.blockchainId = blockchainId;
        this.smartContractPath = smartContractPath;
        this.manager = manager;
        this.dtxManager = dtxManager;
    }

    @JsonRpcMethod
    public String Invoke(
            @JsonRpcParam("signature") MemberSignature signature,
            @JsonRpcParam("inputArguments") List<Argument> inputArguments,
            @JsonRpcParam("outputParams") List<Parameter> outputParams,
            @JsonRpcParam("callbackUrl") String callbackUrl,
            @JsonRpcParam("correlationId") String correlationId,
            @JsonRpcParam("callbackBinding") String callbackBinding,
            @JsonRpcParam("sideEffects") boolean sideEffects,
            @JsonRpcOptional @JsonRpcParam("degreeOfConfidence") double degreeOfConfidence,
            @JsonRpcOptional @JsonRpcParam("timeout") Long timeout,
            @JsonRpcOptional @JsonRpcParam("nonce") Long nonce,
            @JsonRpcOptional @JsonRpcParam("digitalSignature") String digitalSignature
    ) {
        log.info("SCIP Invoke method is executed!");
        List<Parameter> inputs = inputArguments.stream()
                .map(arg -> Parameter.builder()
                        .name(arg.getName())
                        .value(arg.getValue())
                        .type(signature.getParameters().stream().filter(p -> p.getName().equals(arg.getName())).map(Parameter::getType).findFirst().orElse(""))
                        .build())
                .toList();

        if (inputs.stream().anyMatch(p -> p.getName().equals(DTX_ID_FIELD_NAME))) {
            dtxManager.invokeSc(blockchainId, smartContractPath, signature.getName(), inputs, outputParams,
                    degreeOfConfidence, callbackBinding, sideEffects, nonce, callbackUrl, timeout, correlationId, digitalSignature);
        } else {
            manager.invokeSmartContractFunction(blockchainId, smartContractPath, signature.getName(), inputs, outputParams,
                    degreeOfConfidence, callbackBinding, sideEffects, nonce, callbackUrl, timeout, correlationId, digitalSignature);
        }

        return "OK";
    }

    @JsonRpcMethod
    public String Subscribe(
            @JsonRpcParam("signature") MemberSignature memberSignature,
            @JsonRpcParam("callbackUrl") String callbackUrl,
            @JsonRpcParam("correlationId") String correlationId,
            @JsonRpcParam("callbackBinding") String callbackBinding,
            @JsonRpcOptional @JsonRpcParam("degreeOfConfidence") double degreeOfConfidence,
            @JsonRpcOptional @JsonRpcParam("filter") String filter
    ) {
        log.info("SCIP Subscribe method is executed!");

        if (!memberSignature.isFunction()) {
            manager.subscribeToEvent(blockchainId, smartContractPath, memberSignature.getName(), memberSignature.getParameters(), degreeOfConfidence, filter, callbackBinding, callbackUrl, correlationId);
        } else {
            log.error("Not all SCIP adapters support subscribing to function occurrences. Cannot process request!");
            throw new InvalidScipParameterException();
        }

        return "OK";
    }

    @JsonRpcMethod
    public String Unsubscribe(@JsonRpcOptional @JsonRpcParam("signature") MemberSignature memberSignature,
                              @JsonRpcParam("correlationId") String correlationId) {
        log.info("SCIP Unsubscribe method is executed!");

        if (memberSignature.isFunction()) {
            manager.cancelFunctionSubscriptions(blockchainId, smartContractPath, correlationId, memberSignature.getName(), memberSignature.getParameters());
        } else {
            manager.cancelEventSubscriptions(blockchainId, smartContractPath, correlationId, memberSignature.getName(), memberSignature.getParameters());
        }

        return "OK";
    }

    @JsonRpcMethod
    public QueryResult Query(
            @JsonRpcOptional @JsonRpcParam("signature") MemberSignature memberSignature,
            @JsonRpcOptional @JsonRpcParam("filter") String filter,
            @JsonRpcOptional @JsonRpcParam("timeframe") TimeFrame timeFrame) {
        log.info("SCIP Query method is executed!");

        if (!memberSignature.isFunction()) {
            return manager.queryEvents(blockchainId, smartContractPath, memberSignature.getName(), memberSignature.getParameters(), filter, timeFrame);
        } else {
            log.error("Not all SCIP adapters support subscribing to function occurrences. Cannot process request!");
            throw new InvalidScipParameterException();
        }

    }

    @JsonRpcMethod
    public String Start_Dtx() {
        log.info("SCIP-T Start_Dtx method is executed!");

        return dtxManager.startDtx().toString();
    }

    @JsonRpcMethod
    public String Commit_Dtx(@JsonRpcParam(DTX_ID_FIELD_NAME) String dtxId) {
        log.info("SCIP-T Commit_Dtx method is executed!");
        UUID uuid = UUID.fromString(dtxId);
        dtxManager.commitDtx(uuid);

        return "OK";
    }

    @JsonRpcMethod
    public String Abort_Dtx(@JsonRpcParam(DTX_ID_FIELD_NAME) String dtxId) {
        log.info("SCIP-T Abort_Dtx method is executed!");
        UUID uuid = UUID.fromString(dtxId);
        dtxManager.abortDtx(uuid);

        return "OK";
    }
}
