/********************************************************************************
 * Copyright (c) 2018-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionKey;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.EnsureTransactionStateRequest;
import com.oracle.js.parser.ir.Block;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;


@RestController()
@RequestMapping("ensure-transaction-state")
@Log4j2
public class EnsureTransactionStateController extends SubscriptionController {

    public EnsureTransactionStateController(BlockchainManager manager) {
        super(manager);
    }

    @GetMapping
    public Collection<SubscriptionKey> get(){

        return getSubscriptions(SubscriptionType.ENSURE_TRANSACTION_STATE);
    }

    @PostMapping(consumes= MediaType.APPLICATION_XML_VALUE)
    public void ensureTransactionState(@RequestBody EnsureTransactionStateRequest request){
        log.info("Received an ensureTransactionState request via REST API");
        manager.ensureTransactionState(request.getSubscriptionId(), request.getTxId(), request.getBlockchainId(),
                request.getRequiredConfidence(), request.getEpUrl());
    }
}
