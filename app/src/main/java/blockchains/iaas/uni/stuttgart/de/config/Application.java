package blockchains.iaas.uni.stuttgart.de.config;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

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
@ApplicationPath("")
public class Application extends ResourceConfig {
    public Application() {
        AdapterManager a = AdapterManager.getInstance();

        BlockchainAdapter b = a.getAdapter("eth-0");
        System.out.println(">>> " + b.testConnection());
        packages("blockchains.iaas.uni.stuttgart.de");
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);

    }
}
