/*******************************************************************************
 * Copyright (c) 2022 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.restapi.controllers;

import java.util.Collection;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import blockchains.iaas.uni.stuttgart.de.restapi.model.common.BindingRule;
import blockchains.iaas.uni.stuttgart.de.restapi.util.UriUtil;
import blockchains.iaas.uni.stuttgart.de.scip.bindings.EndpointBindings;

@Path("bindings")
public class BindingRulesController {

    @Context
    protected UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response acceptRule(BindingRule request) {
        try {
            EndpointBindings.getInstance().addRule(request.getUrl(), request.getBindingId());
            return Response.ok(UriUtil.generateSelfLink(uriInfo)).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.NOT_FOUND).entity(exception.getMessage()).build();
        }
    }

    @DELETE
    public Response deleteRule() {
        final String url = this.uriInfo.getQueryParameters().getFirst("url");
        if (EndpointBindings.getInstance().removeRule(url) == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.noContent().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRules() {
        Collection<BindingRule> list = EndpointBindings
                .getInstance()
                .getAllRules()
                .stream()
                .map(pair -> new BindingRule(pair.left, pair.right))
                .collect(Collectors.toList());
        return Response.ok(list).build();
    }
}
