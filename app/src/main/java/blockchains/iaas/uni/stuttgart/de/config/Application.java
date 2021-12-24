package blockchains.iaas.uni.stuttgart.de.config;


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
//        final PluginManager pluginManager = new DefaultPluginManager() {
//
//            protected ExtensionFinder createExtensionFinder() {
//                DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
//                extensionFinder.addServiceProviderExtensionFinder(); // to activate "HowdyGreeting" extension
//                return extensionFinder;
//            }
//
//        };
//
//        pluginManager.loadPlugins();
//        pluginManager.startPlugins();
//
//        List<IExtensionInterface> greetings = pluginManager.getExtensions(IExtensionInterface.class);
//
//        for (IExtensionInterface greeting : greetings) {
//            System.out.println(">>> " + greeting.getVersion());
//        }

//        final PluginManager pluginManager = new DefaultPluginManager(Paths.get("../plugins")){
//
//            protected ExtensionFinder createExtensionFinder() {
//                DefaultExtensionFinder extensionFinder = (DefaultExtensionFinder) super.createExtensionFinder();
//                extensionFinder.addServiceProviderExtensionFinder(); // to activate "HowdyGreeting" extension
//                return extensionFinder;
//            }
//
//        };
//
//        PluginManager pluginManager = new DefaultPluginManager(Paths.get("../plugins")) {
//
//            @Override
//            protected PluginLoader createPluginLoader() {
//                // load only jar plugins
//                return new JarPluginLoader(this);
//            }

//            @Override
//            protected PluginDescriptorFinder createPluginDescriptorFinder() {
//                // read plugin descriptor from jar's manifest
//                return new ManifestPluginDescriptorFinder();
//            }

//        };
//        System.out.println("Runtime mode: " + pluginManager.getRuntimeMode());
//        pluginManager.loadPlugins();
//        pluginManager.startPlugins();
//
//        List<IExtensionInterface> greetings = pluginManager.getExtensions(IExtensionInterface.class);
//
//        for (IExtensionInterface greeting : greetings) {
//            System.out.println(">>> " + greeting.getVersion());
//        }


        packages("blockchains.iaas.uni.stuttgart.de");
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);

    }
}
