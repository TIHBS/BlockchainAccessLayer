/*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.connectionprofiles;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import blockchains.iaas.uni.stuttgart.de.connectionprofiles.profiles.BitcoinConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.profiles.EthereumConnectionProfile;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.profiles.FabricConnectionProfile;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConnectionProfileManagerTest {
    private void assertProfiles(ConnectionProfilesManager manager) {
        Assertions.assertNotNull(manager);
        Map<String, AbstractConnectionProfile> gatewayMap = manager.getConnectionProfiles();
        Assertions.assertNotNull(gatewayMap);
        Assertions.assertEquals(3, gatewayMap.size());
        Set<String> keySet = gatewayMap.keySet();
        Assertions.assertTrue(keySet.contains("eth-0"));
        Assertions.assertTrue(keySet.contains("btc-0"));
        Assertions.assertTrue(keySet.contains("fabric-0"));
        Assertions.assertTrue(gatewayMap.get("eth-0") instanceof EthereumConnectionProfile);
        Assertions.assertTrue(gatewayMap.get("btc-0") instanceof BitcoinConnectionProfile);
        Assertions.assertTrue(gatewayMap.get("fabric-0") instanceof FabricConnectionProfile);
    }

    @Test
    void testLoadFromFile() throws URISyntaxException {
        final String DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME = "gatewayConfiguration.json";
        final File file = new File(Objects.requireNonNull(
                ConnectionProfileManagerTest.class.getClassLoader().getResource(DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME)).toURI());
        ConnectionProfilesManager manager = ConnectionProfilesManager.getInstance();
        manager.resetConnectionProfiles();
        manager.loadConnectionProfilesFromFile(file);
        assertProfiles(manager);
    }

    @Test
    void testLoadFromString() {
        ConnectionProfilesManager manager = ConnectionProfilesManager.getInstance();
        manager.resetConnectionProfiles();
        String json = "{\n" +
                "  \"eth-0\": {\n" +
                "    \"@type\": \"ethereum\",\n" +
                "    \"nodeUrl\":\"http://localhost:7545\",\n" +
                "    \"keystorePath\":\"C:\\\\Ethereum\\\\keystore\\\\UTC--2019-05-30T11-21-08.970000000Z--90645dc507225d61cb81cf83e7470f5a6aa1215a.json\",\n" +
                "    \"keystorePassword\":\"123456789\",\n" +
                "    \"adversaryVotingRatio\": \"0.2\"\n" +
                "  },\n" +
                "  \"btc-0\" : {\n" +
                "    \"@type\": \"bitcoin\",\n" +
                "    \"rpcProtocol\": \"http\",\n" +
                "    \"rpcHost\": \"129.69.214.211\",\n" +
                "    \"rpcPort\": \"8332\",\n" +
                "    \"rpcUser\": \"falazigb\",\n" +
                "    \"rpcPassword\": \"123456789\",\n" +
                "    \"httpAuthScheme\": \"Basic\",\n" +
                "    \"notificationAlertPort\": \"5158\",\n" +
                "    \"notificationBlockPort\": \"5159\",\n" +
                "    \"notificationWalletPort\": \"5160\",\n" +
                "    \"adversaryVotingRatio\": \"0.1\"\n" +
                "  },\n" +
                "  \"fabric-0\" : {\n" +
                "    \"@type\": \"fabric\",\n" +
                "    \"walletPath\": \"C:\\\\Users\\\\falazigb\\\\Documents\\\\GitHub\\\\fabric\\\\fabric-samples\\\\fabcar\\\\javascript\\\\wallet\",\n" +
                "    \"userName\": \"user1\",\n" +
                "    \"connectionProfilePath\": \"C:\\\\Users\\\\falazigb\\\\Documents\\\\GitHub\\\\fabric\\\\fabric-samples\\\\first-network\\\\connection-org1.json\"\n" +
                "  }\n" +
                "}";
        manager.loadConnectionProfilesFromJson(json);
        assertProfiles(manager);
    }

    @Test
    void testInitialFile() throws IOException {
        // just in case :)
        ConnectionProfilesManager.initialConfigurationFilePath.toFile().delete();
        ConnectionProfilesManager.resetInstance();
        ConnectionProfilesManager manager = ConnectionProfilesManager.getInstance();
        Assertions.assertEquals(0, manager.getConnectionProfiles().size());
        final String DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME = "gatewayConfiguration.json";

        try (InputStream stream = ConnectionProfileManagerTest.class.getClassLoader().getResourceAsStream(
                DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME)) {
            FileUtils.copyInputStreamToFile(Objects.requireNonNull(stream), ConnectionProfilesManager.initialConfigurationFilePath.toFile());
        }

        ConnectionProfilesManager.resetInstance();
        manager = ConnectionProfilesManager.getInstance();
        Assertions.assertEquals(3, manager.getConnectionProfiles().size());
        ConnectionProfilesManager.initialConfigurationFilePath.toFile().delete();
    }
}