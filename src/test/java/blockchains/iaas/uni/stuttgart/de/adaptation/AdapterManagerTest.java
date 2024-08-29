/*******************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.adaptation;

import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BlockchainIdNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdapterManagerTest {
    @Test
    void testRecreation() {
        String version1 = "{\"eth-0\": {\n" +
                "    \"@type\": \"ethereum\",\n" +
                "    \"nodeUrl\":\"http://localhost:7545\",\n" +
                "    \"keystorePath\":\"C:\\\\Ethereum\\\\keystore\\\\UTC--2019-05-30T11-21-08.970000000Z--90645dc507225d61cb81cf83e7470f5a6aa1215a.json\",\n" +
                "    \"keystorePassword\":\"123456789\",\n" +
                "    \"adversaryVotingRatio\": \"0.2\"\n" +
                "  }}";
        String version2 = "{\"eth-0\": {\n" +
                "    \"@type\": \"ethereum\",\n" +
                "    \"nodeUrl\":\"http://localhost:7545\",\n" +
                "    \"keystorePath\":\"C:\\\\Ethereum\\\\keystore\\\\UTC--2019-05-30T11-21-08.970000000Z--90645dc507225d61cb81cf83e7470f5a6aa1215a.json\",\n" +
                "    \"keystorePassword\":\"123456789\",\n" +
                "    \"adversaryVotingRatio\": \"0.3\"\n" +
                "  }}";

        ConnectionProfilesManager.getInstance().loadConnectionProfilesFromJson(version1);
        BlockchainAdapter adapter1 = AdapterManager.getInstance().getAdapter("eth-0");
        assertNotNull(adapter1);
        assertThrows(BlockchainIdNotFoundException.class, ()->AdapterManager.getInstance().getAdapter("eth-1"));
        BlockchainAdapter adapter2 = AdapterManager.getInstance().getAdapter("eth-0");
        Assertions.assertEquals(adapter1, adapter2);
        ConnectionProfilesManager.getInstance().loadConnectionProfilesFromJson(version2);
        BlockchainAdapter adapter3 = AdapterManager.getInstance().getAdapter("eth-0");
        assertNotNull(adapter3);
        assertNotEquals(adapter1, adapter3);
    }

}