/*******************************************************************************
 * Copyright (c) 2022-2024 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Akshay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.adaptation;

import blockchains.iaas.uni.stuttgart.de.api.IAdapterExtension;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.api.model.LinearChainTransaction;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainPluginManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class TestLoadAdapter {
    // Add -Dpf4j.pluginsDir=<plugin_storage_path> -DethereumPluginJarPath=<path_to_ethereum_plugin_jar>

    private static final Logger log = LoggerFactory.getLogger(TestLoadAdapter.class);
    private static final String NETWORK_NAME = "eth-0";
    private static final double REQUIRED_CONFIDENCE = 0.6;
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

        assertEquals(0, pluginManager.getPlugins().size());

        Path uploadedPluginPath = Paths.get(pluginManager.getPluginsPath() + "/ethereum.jar");

        Files.copy(pluginPath, uploadedPluginPath);

        pluginManager.loadJar(pluginPath);
        assertEquals(1, pluginManager.getPlugins().size());

        PluginWrapper pluginWrapper = pluginManager.getPlugins().get(0);
        String pluginId = pluginWrapper.getPluginId();

        assertEquals("ethereum-plugin", pluginId);

        assertEquals(PluginState.RESOLVED, pluginManager.getPluginState(pluginId));
        pluginManager.startPlugin(pluginId);

        assertEquals(PluginState.STARTED, pluginManager.getPluginState(pluginId));

    }

    @Test
    public void testSendEthereumTransaction() throws IOException, URISyntaxException, ExecutionException, InterruptedException {

        Path uploadedPluginPath = Paths.get(pluginManager.getPluginsPath() + "/ethereum.jar");
        Files.copy(pluginPath, uploadedPluginPath);
        pluginManager.loadJar(pluginPath);

        String pluginId = pluginManager.getPlugins().get(0).getPluginId();
        pluginManager.startPlugin(pluginId);

        assert pluginManager.getPluginState(pluginId) == PluginState.STARTED;

        List<IAdapterExtension> adapterExtensions = pluginManager.getExtensions();
        assert adapterExtensions.size() == 1;

        final String DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME = "gatewayConfiguration.json";

        final File file = new File(Objects.requireNonNull(
                TestLoadAdapter.class.getClassLoader().getResource(DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME)).toURI());

        ConnectionProfilesManager manager = ConnectionProfilesManager.getInstance();
        manager.resetConnectionProfiles();
        manager.loadConnectionProfilesFromFile(file);

        BlockchainAdapter adapter = AdapterManager.getInstance().getAdapter(NETWORK_NAME);
        assertEquals("true", adapter.testConnection());

        final String toAddress = "0x182761AC584C0016Cdb3f5c59e0242EF9834fef0";
        final BigDecimal value = new BigDecimal(5000);
        LinearChainTransaction result = (LinearChainTransaction) adapter.submitTransaction(toAddress, value, REQUIRED_CONFIDENCE).get();
        log.debug("transaction hash is: " + result.getTransactionHash());
    }

    private void clearPluginDirectory() {
        Path path = pluginManager.getPluginsPath();
        final File[] files = path.toFile().listFiles();
        for (File f : files) f.delete();
    }

    private Map<String, Object> getConfiguration() {
        Map<String, Object> map = new HashMap<>();
        map.put("nodeUrl", "http://localhost:7545/");
        map.put("keystorePath", "/account.json");
        map.put("averageBlockTimeSeconds", "2");
        map.put("keystorePassword", "123456789");
        map.put("adversaryVotingRatio", 0.2);
        map.put("pollingTimeSeconds", 2);

        return map;
    }

    /*
    * Creating account.json file using node 16
    *
     ```bash
        npm install web3
     ```

     ```javascript
        var Web3 = require('web3');
        var web3 = new Web3(Web3.givenProvider || 'ws://localhost:7545');
        var privateKey="<your_private_key>";
        var password="123456789";
        var JsonWallet = web3.eth.accounts.encrypt(privateKey, password);
        console.log(JSON.stringify(JsonWallet)
     ```
     Copy the output to a file `account.json` on the machine.
    * */
}
