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

package blockchains.iaas.uni.stuttgart.de.jsonrpc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.InvalidScipParameterException;
import blockchains.iaas.uni.stuttgart.de.jsonrpc.model.MemberSignature;
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
import lombok.extern.log4j.Log4j2;

@JsonRpcService
@Log4j2
public class BalService {
    private final String blockchainType;
    private final String blockchainId;
    private final String smartContractPath;
    private final BlockchainManager manager;
    private final DistributedTransactionManager dtxManager;
    private static final String DTX_ID_FIELD_NAME = "dtx_id";

    public BalService(String blockchainType, String blockchainId, String smartContractPath, BlockchainManager manager, DistributedTransactionManager dtxManager) {
        this.blockchainType = blockchainType;
        this.blockchainId = blockchainId;
        this.smartContractPath = smartContractPath;
        this.manager = manager;
        this.dtxManager = dtxManager;
    }

    @JsonRpcMethod
    public String Invoke(
            @JsonRpcParam("signature") MemberSignature memberSignature,
            @JsonRpcParam("inputArguments") List<Parameter> inputs,
            @JsonRpcParam("outputParams") List<Parameter> outputs,
            @JsonRpcParam("callbackUrl") String callbackUrl,
            @JsonRpcParam("correlationId") String correlationId,
            @JsonRpcParam("callbackBinding") String callbackBinding,
            @JsonRpcParam("sideEffects") boolean sideEffects,
            @JsonRpcOptional @JsonRpcParam("degreeOfConfidence") double requiredConfidence,
            @JsonRpcOptional @JsonRpcParam("timeout") Long timeoutMillis,
            @JsonRpcOptional @JsonRpcParam("Nonce") Long nonce,
            @JsonRpcOptional @JsonRpcParam("digitalSignature") String digitalSignature
    ) {
        log.info("SCIP Invoke method is executed!");

        for(Parameter input : inputs) {
            Optional<Parameter> fromSig = memberSignature.getParameters().stream().filter(p -> p.getName().equals(input.getName())).findFirst();
            input.setType(fromSig.orElseThrow(InvalidScipParameterException::new).getType());
        }

        if (inputs.stream().anyMatch(p -> p.getName().equals(DTX_ID_FIELD_NAME))) {
            dtxManager.invokeSc(blockchainId, smartContractPath, memberSignature.getName(), inputs, outputs,
                    requiredConfidence, callbackUrl, timeoutMillis, correlationId, digitalSignature);
        } else {
            manager.invokeSmartContractFunction(blockchainId, smartContractPath, memberSignature.getName(), inputs, outputs,
                    requiredConfidence, callbackUrl, timeoutMillis, correlationId, digitalSignature);
        }

        return "OK";
    }

    @JsonRpcMethod
    public String Subscribe(
            @JsonRpcParam("signature") MemberSignature memberSignature,
            @JsonRpcParam("callbackUrl") String callbackUrl,
            @JsonRpcParam("correlationId") String correlationId,
            @JsonRpcOptional @JsonRpcParam("degreeOfConfidence") double degreeOfConfidence,
            @JsonRpcOptional @JsonRpcParam("filter") String filter
            ) {
        log.info("SCIP Subscribe method is executed!");


        if (!memberSignature.isFunction()) {
            manager.subscribeToEvent(blockchainId, smartContractPath, memberSignature.getName(), memberSignature.getParameters(), degreeOfConfidence, filter, callbackUrl, correlationId);
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
