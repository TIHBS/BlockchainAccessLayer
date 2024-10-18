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
package blockchains.iaas.uni.stuttgart.de.restapi.controllers;


import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionKey;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.SubmitTransactionRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController()
@RequestMapping("submit-transaction")
@Log4j2
public class SubmitTransactionController extends SubscriptionController {

    public SubmitTransactionController(BlockchainManager manager) {
        super(manager);
    }

    @GetMapping
    public Collection<SubscriptionKey> get() {
        return getSubscriptions(SubscriptionType.SUBMIT_TRANSACTION);
    }

    @PostMapping(consumes = org.springframework.http.MediaType.APPLICATION_XML_VALUE)
    public void submitTransaction(SubmitTransactionRequest request) {
        log.info("Received an submitTransaction request via REST API");
        manager.submitNewTransaction(request.getSubscriptionId(), request.getTo(), request.getValue(), request.getBlockchainId(),
                request.getRequiredConfidence(), request.getEpUrl());
    }

    @PostMapping(path = "/dummy")
    public ResponseEntity<String> dummyEndPoint(@RequestBody Object remoteResponse) {
        log.info("dummy path received the following response: {}", remoteResponse);
        return ResponseEntity.accepted().build();
    }
}
