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

package blockchains.iaas.uni.stuttgart.de.restapi.controllers;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import blockchains.iaas.uni.stuttgart.de.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import com.fasterxml.jackson.core.JsonProcessingException;

@Path("configure")
public class ConnectionProfilesController {
    @Context
    protected UriInfo uriInfo;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void acceptConfiguration(Map<String, AbstractConnectionProfile> profiles) {
        ConnectionProfilesManager.getInstance().loadConnectionProfiles(profiles);
    }

    @DELETE
    public void resetConfigurations() {
        ConnectionProfilesManager.getInstance().resetConnectionProfiles();
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
