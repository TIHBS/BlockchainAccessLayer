package blockchains.iaas.uni.stuttgart.de.config;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import javax.ws.rs.ApplicationPath;
import java.util.List;

/********************************************************************************
 * Copyright (c) 2018-2023 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-Author: Akshay Patel
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
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);
        packages("blockchains.iaas.uni.stuttgart.de");

        // Required to load the plugins at startup
        if (Boolean.getBoolean("enablePluginsAtStart")) {
            BlockchainPluginManager blockchainPluginManager = BlockchainPluginManager.getInstance();
            blockchainPluginManager.startPlugins();
            List<PluginWrapper> plugins = blockchainPluginManager.getPlugins(PluginState.STARTED);

            for (PluginWrapper pluginWrapper : plugins) {
                blockchainPluginManager.registerConnectionProfileSubtypeClass(pluginWrapper.getPluginId());
            }
        }
    }
}
