/********************************************************************************
 * Copyright (c) 2022 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Akshay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.management;

import blockchains.iaas.uni.stuttgart.de.Constants;
import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtension;
import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.config.ObjectMapperProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import org.pf4j.*;

import java.nio.file.Path;
import java.util.List;

public class BlockchainPluginManager {

    private PluginManager pluginManager = null;
    private static BlockchainPluginManager instance = null;

    private BlockchainPluginManager() {

        this.pluginManager = new DefaultPluginManager(Constants.PLUGINS_DIRECTORY) {
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
    }

    public static BlockchainPluginManager getInstance() {
        if (instance == null) {
            instance = new BlockchainPluginManager();
        }
        return instance;
    }

    public List<IAdapterExtension> getExtensions() {
        return this.pluginManager.getExtensions(IAdapterExtension.class);
    }

    public void loadJar(Path path) {
        pluginManager.loadPlugin(path);
    }

    public Path getPluginsPath() {
        return Constants.PLUGINS_DIRECTORY;
    }

    public List<PluginWrapper> getPlugins() {
        return pluginManager.getPlugins();
    }

    public void unloadPlugin(String pluginId) {
        pluginManager.unloadPlugin(pluginId);
    }

    public void startPlugin(String pluginId) {
        pluginManager.startPlugin(pluginId);


    }

    public void disablePlugin(String pluginId) {
        pluginManager.disablePlugin(pluginId);
    }

    public void deletePlugin(String pluginId) {
        pluginManager.deletePlugin(pluginId);
    }

    public void enablePlugin(String pluginId) {
        pluginManager.enablePlugin(pluginId);
    }

    public PluginState getPluginState(String pluginId) {
        return pluginManager.getPlugin(pluginId).getPluginState();
    }

    public void registerConnectionProfileSubtypeClass(ObjectMapper objectMapper, String pluginId) {
        List<IAdapterExtension> adapterExtensions = this.pluginManager.getExtensions(IAdapterExtension.class, pluginId);
        for (IAdapterExtension adapterExtension : adapterExtensions) {
            // TODO: String typeName = adapterExtension.getConnectionProfileNamedType();
            objectMapper.registerSubtypes(new NamedType(adapterExtension.getConnectionProfileClass(), "ethereum"));
        }
    }
}
