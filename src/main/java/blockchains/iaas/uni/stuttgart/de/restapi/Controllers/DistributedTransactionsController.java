/********************************************************************************
 * Copyright (c) 2023 Institute for the Architecture of Application System -
 * University of Stuttgart
 * 
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.management.model.DistributedTransaction;
import blockchains.iaas.uni.stuttgart.de.management.tccsci.DistributedTransactionRepository;
import blockchains.iaas.uni.stuttgart.de.restapi.model.response.LinkCollectionResponse;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

@Path("distributed-transactions")
public class DistributedTransactionsController {

    @Context
    protected UriInfo uriInfo;
    @GET
    public Response get() {
        List<DistributedTransaction> all = DistributedTransactionRepository.getInstance().getAll();
        final LinkCollectionResponse response = new LinkCollectionResponse();

        for (final DistributedTransaction dtx : all) {
            response.add(UriUtil.generateSubResourceLink(uriInfo, dtx.getId().toString(), false, "self"));
        }

        return Response.ok(response).build();
    }

    @GET
    @Path("/{dtxId}")
    public Response getSubscriptionDetails(@PathParam("dtxId") final String dtxId) {
        UUID uuid = UUID.fromString(dtxId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(uuid);
        if (dtx != null) {
            return Response
                    .status(Response.Status.OK)
                    .entity(dtx)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
