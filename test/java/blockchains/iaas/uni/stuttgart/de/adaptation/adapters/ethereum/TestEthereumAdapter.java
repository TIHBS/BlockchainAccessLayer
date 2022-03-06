/*******************************************************************************
 * Copyright (c) 2022 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Akshay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.adaptation.adapters.ethereum;

import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.*;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestEthereumAdapter {

    // Add -Dpf4j.pluginsDir=<plugin_storage_path> -DethereumPluginJarPath=<path_to_ethereum_plugin_jar>

    private final BlockchainPluginManager pluginManager = BlockchainPluginManager.getInstance();
    private final Path pluginPath = Paths.get(System.getProperty("ethereumPluginJarPath"));

    @BeforeEach
    public void setUp() {
        clearPluginDirectory();
    }

    @AfterEach
    public void tearDown() {
        clearPluginDirectory();
    }

    @Test
    public void testEthereumAdapter() throws IOException {

        // Basic tests

        assert pluginManager.getPlugins().size() == 0;

        Path uploadedPluginPath = Paths.get(pluginManager.getPluginPath() + "/ethereum.jar");

        Files.copy(pluginPath, uploadedPluginPath);

        pluginManager.loadJar(pluginPath);
        Assertions.assertEquals(1, pluginManager.getPlugins().size());

        PluginWrapper pluginWrapper = pluginManager.getPlugins().get(0);
        String pluginId = pluginWrapper.getPluginId();

        Assertions.assertEquals("ethereum-plugin", pluginId);

        Assertions.assertEquals(PluginState.RESOLVED, pluginManager.getPluginState(pluginId));
        pluginManager.startPlugin(pluginId);

        Assertions.assertEquals(PluginState.STARTED, pluginManager.getPluginState(pluginId));

    }


    private void clearPluginDirectory() {
        Path path = pluginManager.getPluginPath();
        final File[] files = path.toFile().listFiles();
        for (File f : files) f.delete();
    }
}
