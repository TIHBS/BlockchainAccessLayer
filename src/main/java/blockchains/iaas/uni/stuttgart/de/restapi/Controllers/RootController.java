package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import blockchains.iaas.uni.stuttgart.de.restapi.model.response.LinkCollectionResponse;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

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
@Path("/")
public class RootController {
    @Context
    protected UriInfo uriInfo;

    @GET
    public Response getSubscriptions(){
        LinkCollectionResponse response = new LinkCollectionResponse();
        response.add(UriUtil.generateSubResourceLink(uriInfo, "submit-transaction", false, "submit-transaction"));
        response.add(UriUtil.generateSubResourceLink(uriInfo, "receive-transaction", false, "receive-transaction"));
        response.add(UriUtil.generateSubResourceLink(uriInfo, "receive-transactions", false, "receive-transactions"));
        response.add(UriUtil.generateSubResourceLink(uriInfo, "ensure-transaction-state", false, "ensure-transaction-state"));
        response.add(UriUtil.generateSubResourceLink(uriInfo, "detect-orphaned-transaction", false, "detect-orphaned-transaction"));

        return Response.ok(response).build();
    }
}
