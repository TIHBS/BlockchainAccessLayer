package blockchains.iaas.uni.stuttgart.de.config;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtenstion;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.pf4j.*;

import javax.ws.rs.ApplicationPath;
import java.nio.file.Paths;
import java.util.List;

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
        String pluginPath = "/home/ash/software/apache-tomcat-8.5.73/plugins";
        PluginManager pluginManager = new DefaultPluginManager(Paths.get(pluginPath)) {
            //
            @Override
            protected PluginLoader createPluginLoader() {
                // load only jar plugins
                return new JarPluginLoader(this);
            }

            @Override
            protected PluginDescriptorFinder createPluginDescriptorFinder() {
                // read plugin descriptor from jar's manifest
                return new ManifestPluginDescriptorFinder();
            }

        };

        System.out.println("Runtime mode: " + pluginManager.getRuntimeMode());
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        List<IAdapterExtenstion> greetings = pluginManager.getExtensions(IAdapterExtenstion.class);

        for (IAdapterExtenstion greeting : greetings) {
            BlockchainAdapter a = greeting.getAdapter("http://localhost:7545", 2);
           System.out.println(">>> " + a.testConnection());
        }

        packages("blockchains.iaas.uni.stuttgart.de");
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);

    }
}
