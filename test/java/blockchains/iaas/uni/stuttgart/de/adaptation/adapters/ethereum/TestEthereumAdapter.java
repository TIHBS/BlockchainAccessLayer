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

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtension;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.jupiter.api.*;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestEthereumAdapter {

    // Add -Dpf4j.pluginsDir=<plugin_storage_path> -DethereumPluginJarPath=<path_to_ethereum_plugin_jar>
    private static final String NETWORK_NAME = "eth-0";

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
    public void testLoadEthereumPlugin() throws IOException {

        // Basic test to check if plugin is loaded correctly

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

    @Test
    public void testCreateAdapterInstanceFromConnectionProfile() throws IOException, URISyntaxException {

        Path uploadedPluginPath = Paths.get(pluginManager.getPluginPath() + "/ethereum.jar");
        Files.copy(pluginPath, uploadedPluginPath);
        pluginManager.loadJar(pluginPath);

        String pluginId = pluginManager.getPlugins().get(0).getPluginId();
        pluginManager.startPlugin(pluginId);

        assert pluginManager.getPluginState(pluginId) == PluginState.STARTED;

        List<IAdapterExtension> adapterExtensions = pluginManager.getExtensions();
        assert adapterExtensions.size() == 1;

        final String DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME = "gatewayConfiguration.json";

        final File file = new File(Objects.requireNonNull(
                TestEthereumAdapter.class.getClassLoader().getResource(DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME)).toURI());

        ConnectionProfilesManager manager = ConnectionProfilesManager.getInstance();
        manager.resetConnectionProfiles();
        manager.loadConnectionProfilesFromFile(file);

        BlockchainAdapter adapter = AdapterManager.getInstance().getAdapter(NETWORK_NAME);

        // blockchainAdapter.invokeSmartContract();
    }

    private void clearPluginDirectory() {
        Path path = pluginManager.getPluginPath();
        final File[] files = path.toFile().listFiles();
        for (File f : files) f.delete();
    }

    private Map<String, String> getConfiguration() {
        Map<String, String> map = new HashMap<>();
        map.put("nodeUrl", "http://localhost:7545/");
        map.put("keystorePath", "C:\\Ethereum\\keystore\\UTC--2019-05-30T11-21-08.970000000Z--90645dc507225d61cb81cf83e7470f5a6aa1215a.json");
        map.put("averageBlockTimeSeconds", "2");
        map.put("keystorePassword", "123456789");
        map.put("adversaryVotingRatio", "0.2");
        map.put("pollingTimeSeconds", "2");

        return map;
    }
}
