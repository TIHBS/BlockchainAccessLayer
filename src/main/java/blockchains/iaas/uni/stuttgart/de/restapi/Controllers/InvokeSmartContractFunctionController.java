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
package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.restapi.model.request.InvokeSmartContractFunctionRequest;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("invoke-smart-contract-function")
public class InvokeSmartContractFunctionController extends SubscriptionController {
    private static final Logger log = LoggerFactory.getLogger(InvokeSmartContractFunctionController.class);

    @GET
    public Response get() {
        return getSubscriptions(SubscriptionType.INVOKE_SMART_CONTRACT_FUNCTION, uriInfo);
    }

    // todo try if still working
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response invokeSCFunction(InvokeSmartContractFunctionRequest request) {
        final BlockchainManager manager = new BlockchainManager();
        manager.invokeSmartContractFunction(
                request.getBlockchainId(),
                request.getSmartContractPath(),
                request.getFunctionIdentifier(),
                request.getInputs().getArguments(),
                request.getOutputs().getArguments(),
                request.getConfidence(),
                request.getEpUrl(),
                request.getTimeoutMillis(),
                request.getSubscriptionId(),
                request.getSignature()
        );

        return Response.created(UriUtil.generateSubResourceURI(this.uriInfo, request.getSubscriptionId(), false))
                .build();
    }
}
