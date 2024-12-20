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
import blockchains.iaas.uni.stuttgart.de.api.connectionprofiles.AbstractConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.plugin.PluginManager;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@Log4j2
public class TestLoadAdapter {
    private static final String NETWORK_NAME = "eth-0";
    private static String originalPf4jPath;
    private final Path pluginPath = Paths.get(Objects.requireNonNull(TestLoadAdapter.class.getClassLoader().getResource("plugins/ethereum.jar")).toURI());
    private final static String KEYSTORE_PATH_KEY = "ethereum.keystorePath";
    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private AdapterManager adapterManager;

    public TestLoadAdapter() throws URISyntaxException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        clearPluginDirectory();
    }

    private void loadPlugin() throws IOException {
        if (pluginManager.getPlugins().stream().filter(p -> "ethereum-plugin".equals(p.getPluginId())).findAny().isEmpty()) {
            Path uploadedPluginPath = pluginManager.getPluginsPath().resolve("ethereum.jar");
            log.info("Loading Plugin: Copying {} to {}...", pluginPath, uploadedPluginPath);
            Files.createDirectories(pluginManager.getPluginsPath());
            Files.copy(pluginPath, uploadedPluginPath);
            pluginManager.loadJar(pluginPath);
        }
    }

    @Test
    public void testLoadEthereumPlugin() throws IOException {
        // Basic test to check if plugin is loaded correctly
        assertEquals(0, pluginManager.getPlugins().size());
        loadPlugin();
        assertEquals(1, pluginManager.getPlugins().size());
        PluginWrapper pluginWrapper = pluginManager.getPlugins().get(0);
        String pluginId = pluginWrapper.getPluginId();
        assertEquals("ethereum-plugin", pluginId);
        assertEquals(PluginState.RESOLVED, pluginManager.getPluginState(pluginId));
        pluginManager.startPlugin(pluginId);
        assertEquals(PluginState.STARTED, pluginManager.getPluginState(pluginId));
    }

    @Test
    public void testLoadConnectionProfile() throws IOException, URISyntaxException {
        loadPlugin();
        String pluginId = pluginManager.getPlugins().get(0).getPluginId();
        pluginManager.startPlugin(pluginId);
        assertEquals(PluginState.STARTED, pluginManager.getPluginState(pluginId));
        List<IAdapterExtension> adapterExtensions = pluginManager.getExtensions();
        assertEquals(1, adapterExtensions.size());
        final String DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME = "gatewayConfiguration.json";
        final File file = new File(Objects.requireNonNull(
                TestLoadAdapter.class.getClassLoader().getResource(DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME)).toURI());
        ConnectionProfilesManager manager = ConnectionProfilesManager.getInstance();
        manager.resetConnectionProfiles();
        assertEquals(0, manager.getConnectionProfiles().size());
        manager.loadConnectionProfilesFromFile(file);
        assertEquals(1, manager.getConnectionProfiles().size());
        AbstractConnectionProfile profile = manager.getConnectionProfiles().get(NETWORK_NAME);
        assertNotNull(profile);
        // we manually set the keystore path to the file included in the test resources (to minimize external dependencies).
        assertNotNull(profile.getProperty(KEYSTORE_PATH_KEY));
        profile.setProperty(KEYSTORE_PATH_KEY,
                Paths.get(this.getClass().getClassLoader().getResource("").toURI())
                        .resolve(profile.getProperty(KEYSTORE_PATH_KEY).toString()).toString());
        log.debug("New keystorePath property for connection profile {} is: {}", NETWORK_NAME, profile.getProperty("keystorePath"));
        BlockchainAdapter adapter = adapterManager.getAdapter(NETWORK_NAME);
        assertNotNull(adapter);
    }

    private void clearPluginDirectory() throws IOException {
        log.info("Cleaning up plugin directory from potential plugin files: {}", () -> pluginManager.getPluginsPath());
        Path path = pluginManager.getPluginsPath();

        if (Files.exists(path)) {
            try (Stream<Path> files = Files.list(path)) {
                files.forEach(filePath -> {
                    try {
                        if (Files.isRegularFile(filePath)) {
                            log.info("Removing file: {}", filePath);
                            Files.delete(filePath);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

}
