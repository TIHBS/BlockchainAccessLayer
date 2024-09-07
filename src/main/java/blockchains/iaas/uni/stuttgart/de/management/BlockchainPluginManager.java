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
import org.pf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Log4j2
@Component
public class BlockchainPluginManager {
    private PluginManager pluginManager = null;
    private final String pluginDirStr;

    private BlockchainPluginManager(@Value("${" + Constants.PF4J_PLUGIN_DIR_PROPERTY + "}")
                                    String pluginDir) {
        log.info("Initializing Blockchain Plugin Manager: pluginDir={}.", pluginDir);
        this.pluginDirStr = pluginDir;
        Path[] dirPaths = new Path[0];
        Path pluginDirPath = getPluginsPath();

        if (pluginDirPath != null) {
            dirPaths = new Path[]{pluginDirPath};
        }

        this.pluginManager = new DefaultPluginManager(dirPaths) {
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

        if (pluginDirPath == null) {
            log.info("Plugin directory not specified. Not loading plugins at startup.");
        } else {
            log.info("Attempting to load blockchain adapter plugins from: '{}'...", () -> pluginDirPath);
            pluginManager.loadPlugins();
            startPlugins();
        }

    }


    public List<IAdapterExtension> getExtensions() {
        return this.pluginManager.getExtensions(IAdapterExtension.class);
    }

    public void loadJar(Path path) {
        pluginManager.loadPlugin(path);
    }

    public Path getPluginsPath() {
        return pluginDirStr != null ? Paths.get(pluginDirStr) : null;
    }

    public List<PluginWrapper> getPlugins() {
        return pluginManager.getPlugins();
    }

    public void unloadPlugin(String pluginId) {
        pluginManager.unloadPlugin(pluginId);
    }

    public void startPlugin(String pluginId) {
        pluginManager.startPlugin(pluginId);
        registerConnectionProfileSubtypeClass(pluginId);
    }

    public void startPlugins() {
        pluginManager.startPlugins();
        List<PluginWrapper> plugins = getPlugins(PluginState.STARTED);

        for (PluginWrapper pluginWrapper : plugins) {
            registerConnectionProfileSubtypeClass(pluginWrapper.getPluginId());
        }
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

    private void registerConnectionProfileSubtypeClass(String pluginId) {
        List<IAdapterExtension> adapterExtensions = this.pluginManager.getExtensions(IAdapterExtension.class, pluginId);
        for (IAdapterExtension adapterExtension : adapterExtensions) {
            String namedType = adapterExtension.getConnectionProfileNamedType();
            ConnectionProfilesManager.registerConnectionProfileSubtypeClass(adapterExtension.getConnectionProfileClass(), namedType);
        }
    }
}
