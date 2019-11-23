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

import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcParam;
import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcService;
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
}
