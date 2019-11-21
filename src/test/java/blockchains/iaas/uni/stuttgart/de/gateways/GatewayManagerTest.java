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

package blockchains.iaas.uni.stuttgart.de.gateways;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GatewayManagerTest {
    @Test
    void testDeserialization() {
        ConnectionProfilesManager manager = ConnectionProfilesManager.getInstance();
        Assertions.assertNotNull(manager);
        Map<String, AbstractConnectionProfile> gatewayMap = manager.getGateways();
        Assertions.assertNotNull(gatewayMap);
        Assertions.assertEquals(2, gatewayMap.size());
        Set<String> keySet = gatewayMap.keySet();
        Assertions.assertTrue(keySet.contains("eth1"));
        Assertions.assertTrue(keySet.contains("bc1"));
        Assertions.assertTrue(gatewayMap.get("eth1") instanceof EthereumConnectionProfile);
        Assertions.assertTrue(gatewayMap.get("bc1") instanceof BitcoinConnectionProfile);
    }

}