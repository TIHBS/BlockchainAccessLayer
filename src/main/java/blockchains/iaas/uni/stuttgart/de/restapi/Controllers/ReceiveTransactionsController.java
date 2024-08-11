package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionKey;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.ReceiveTransactionsRequest;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/********************************************************************************
 * Copyright (c) 2018 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

@RestController()
@RequestMapping("receive-transactions")
@Log4j2
public class ReceiveTransactionsController extends SubscriptionController {

    @GetMapping
    public Collection<SubscriptionKey> get(){
        return getSubscriptions(SubscriptionType.RECEIVE_TRANSACTIONS);
    }

    @PostMapping(consumes = MediaType.APPLICATION_XML_VALUE)
    public void receiveTransaction(ReceiveTransactionsRequest request){
        log.info("Received an receiveTransaction request via REST API");
        final BlockchainManager manager = new BlockchainManager();
        manager.receiveTransactions(request.getSubscriptionId(), request.getFrom(), request.getBlockchainId(),
                request.getRequiredConfidence(), request.getEpUrl());
    }
}
