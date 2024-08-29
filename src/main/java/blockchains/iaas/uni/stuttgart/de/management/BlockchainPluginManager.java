/********************************************************************************
 * Copyright (c) 2022-2023 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Akshay Patel
 * Co-Author: Ghareeb Falazi
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

import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import lombok.extern.log4j.Log4j2;
import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.JarPluginLoader;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
public class BlockchainPluginManager{


    private PluginManager pluginManager = null;
    private static BlockchainPluginManager instance = null;

    private BlockchainPluginManager() {
        this.pluginManager = new DefaultPluginManager(getPluginsPath()) {
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

        log.info("Attempting to load blockchain adapter plugins from: '{}'...", () -> getPluginsPath());
        pluginManager.loadPlugins();

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
        final String systemProperty = System.getProperty(Constants.PF4j_PLUGIN_DIR_PROPERTY);

        return systemProperty != null ? Paths.get(systemProperty) : null;
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

    public void startPlugins() {
        pluginManager.startPlugins();
    }

    public List<PluginWrapper> getPlugins(PluginState pluginState) {
        return pluginManager.getPlugins(pluginState);
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

    public void registerConnectionProfileSubtypeClass(String pluginId) {
        List<IAdapterExtension> adapterExtensions = this.pluginManager.getExtensions(IAdapterExtension.class, pluginId);
        for (IAdapterExtension adapterExtension : adapterExtensions) {
            String namedType = adapterExtension.getConnectionProfileNamedType();
            ConnectionProfilesManager.registerConnectionProfileSubtypeClass(adapterExtension.getConnectionProfileClass(), namedType);
        }
    }
}
