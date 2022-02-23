package blockchains.iaas.uni.stuttgart.de.restapi.controllers;

import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.DetectOrphanedTransactionRequest;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;

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

@Path("detect-orphaned-transaction")
public class OrphanedTransactionController extends SubscriptionController {

    @GET
    public Response get(){
        return getSubscriptions(SubscriptionType.DETECT_ORPHANED_TRANSACTION, uriInfo);
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response detectOprphanedTransaction(DetectOrphanedTransactionRequest request){
        final BlockchainManager manager = new BlockchainManager();
        manager.detectOrphanedTransaction(request.getSubscriptionId(), request.getTxId(), request.getBlockchainId(),
                 request.getEpUrl());

        return Response.created(UriUtil.generateSubResourceURI(this.uriInfo, request.getSubscriptionId(), false))
                .build();
    }
}
