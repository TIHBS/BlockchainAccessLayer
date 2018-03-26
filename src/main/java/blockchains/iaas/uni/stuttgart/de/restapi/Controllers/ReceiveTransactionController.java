package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.ResourceManager;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.ReceiveTransactionsRequest;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;
import io.swagger.v3.oas.annotations.Operation;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

@Path("receive-transaction")
public class ReceiveTransactionController extends SubscriptionController{

    @GET
    @Operation(summary="gets a list of all receive transaction subscriptions")
    public Response get(){
        return getSubscriptions(SubscriptionType.RECEIVE_TRANSACTION, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response receiveTransaction(ReceiveTransactionsRequest request){
        final ResourceManager manager = new ResourceManager();
        manager.receiveTransaction(request.getSubscriptionId(), request.getFrom(), request.getBlockchainId(),
                request.getWaitFor(), request.getEpUrl());

        return Response.created(UriUtil.generateSubResourceURI(this.uriInfo, request.getSubscriptionId(), false))
                .build();
    }
}
