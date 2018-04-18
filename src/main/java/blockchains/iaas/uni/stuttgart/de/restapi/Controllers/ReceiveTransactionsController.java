package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.ReceiveTransactionsRequest;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

@Path("receive-transactions")
public class ReceiveTransactionsController extends SubscriptionController {

    @GET
    public Response get(){
        return getSubscriptions(SubscriptionType.RECEIVE_TRANSACTIONS, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response receiveTransaction(ReceiveTransactionsRequest request){
        final BlockchainManager manager = new BlockchainManager();
        manager.receiveTransactions(request.getSubscriptionId(), request.getFrom(), request.getBlockchainId(),
                request.getWaitFor(), request.getEpUrl());

        return Response.created(UriUtil.generateSubResourceURI(this.uriInfo, request.getSubscriptionId(), false))
                .build();
    }
}
