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

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcMethod;
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
    public String Invoke() {
        log.info("Invoke method is executed!");
        log.info("blockchain type: {}, blockchain id: {}, smart contract path: {}", blockchainType, blockchainId, smartContractPath);
        return "This is not implemented yet!";
    }
}
