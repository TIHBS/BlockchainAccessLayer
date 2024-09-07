/********************************************************************************
 * Copyright (c) 2018-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.jsonrpc.BalService;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.management.tccsci.DistributedTransactionManager;
import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController()
@RequestMapping("/")
@Log4j2
public class RootController {
    private final BlockchainManager manager;
    private final DistributedTransactionManager distributedTransactionManager;

    public RootController(BlockchainManager manager, DistributedTransactionManager distributedTransactionManager) {
        this.manager = manager;
        this.distributedTransactionManager = distributedTransactionManager;
    }

    @PostMapping
    public ResponseEntity<String> performJsonRpcCall(@RequestBody String jsonRequest,
                                                     @RequestParam(name = "blockchain") final String blockchainType,
                                                     @RequestParam(name = "blockchain-id") final String blockchainId,
                                                     @RequestParam(name = "address") final String smartContractAddress) {
        BalService service = new BalService(blockchainType, blockchainId, smartContractAddress, manager, distributedTransactionManager);
        JsonRpcServer server = new JsonRpcServer();
        String response = server.handle(jsonRequest, service);

        return ResponseEntity.ok(response);
    }

}
