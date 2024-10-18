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
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.ReceiveTransactionsRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;


@RestController()
@RequestMapping("receive-transaction")
@Log4j2
public class ReceiveTransactionController extends SubscriptionController {

    public ReceiveTransactionController(BlockchainManager manager) {
        super(manager);
    }

    @GetMapping
    public Collection<SubscriptionKey> get() {
        return getSubscriptions(SubscriptionType.RECEIVE_TRANSACTION);
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
    public void receiveTransaction(ReceiveTransactionsRequest request) {
        log.info("Received an receiveTransaction request via REST API");
        manager.receiveTransaction(request.getSubscriptionId(), request.getFrom(), request.getBlockchainId(),
                request.getRequiredConfidence(), request.getEpUrl());
    }
}
