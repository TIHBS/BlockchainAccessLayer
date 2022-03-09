/*******************************************************************************
 * Copyright (c) 2019-2022 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-author: Akdhay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.Controllers;

import java.util.Map;

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

import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("configure")
public class ConnectionProfilesController {

    private static final Logger log = LoggerFactory.getLogger(ConnectionProfilesController.class);

    @Context
    protected UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void acceptConfiguration(Map<String, Map<String, Object>> profiles) {
        ConnectionProfilesManager.getInstance().loadConnectionProfiles(profiles);
    }

    @DELETE
    public Response resetConfigurations() {
        ConnectionProfilesManager.getInstance().resetConnectionProfiles();
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getConfigurations() throws JsonProcessingException {
        return ConnectionProfilesManager.getInstance().getConnectionProfilesAsJson();

    }

    @Path("test")
    @GET
    public String testConnection() {
        String blockchainId = this.uriInfo.getQueryParameters().getFirst("blockchain-id");
        return (new BlockchainManager()).testConnection(blockchainId);
    }
}
