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

package blockchains.iaas.uni.stuttgart.de.adaptation.adapters.fabric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * In order for this test to succeed:
 * 1- follow the steps of running the first Fabric tutorial at: https://hyperledger-fabric.readthedocs.io/en/release-1.4/write_first_app.html (use the javascript smart contract)
 * 2- execute the enrollAdmin.js and the registerUser.js node programs
 * 3- alter the local hosts file by adding the following entries:
 * 127.0.0.1	orderer.example.com
 * 127.0.0.1	peer0.org1.example.com
 * 127.0.0.1	peer0.org2.example.com
 * 127.0.0.1	peer1.org1.example.com
 * 127.0.0.1	peer1.org2.example.com
 *
 * This ensures that the SDK is able to find the orderer and network peers.
 */
@Disabled
class FabricAdapterTest {
    private static final String NETWORK_NAME = "fabric-0";
    private static final String CHANNEL_NAME = "mychannel";
    private static final String CHAINCODE_NAME = "fabcar";
    private static final Logger log = LoggerFactory.getLogger(FabricAdapterTest.class);

    private FabricAdapter adapter;

    @BeforeEach
    public void initialize() {
        String connectionProfile = "{ \"fabric-0\" : {\n" +
                "    \"@type\": \"fabric\",\n" +
                "    \"walletPath\": \"C:\\\\Users\\\\falazigb\\\\Documents\\\\GitHub\\\\fabric\\\\fabric-samples\\\\ems\\\\javascript\\\\wallet\",\n" +
                "    \"userName\": \"user1\",\n" +
                "    \"connectionProfilePath\": \"C:\\\\Users\\\\falazigb\\\\Documents\\\\GitHub\\\\fabric\\\\fabric-samples\\\\first-network\\\\connection-org1.json\"\n" +
                "  }}";
        ConnectionProfilesManager.getInstance().loadConnectionProfilesFromJson(connectionProfile);
        adapter = (FabricAdapter) AdapterManager.getInstance().getAdapter(NETWORK_NAME);
    }

    @Test
    public void testRoundTrip() throws ExecutionException, InterruptedException {
        String path = String.format("%s/%s", CHANNEL_NAME, CHAINCODE_NAME);
        final String id = String.format("CAR%s", RandomStringUtils.randomNumeric(6));
        this.adapter.invokeSmartContract(
                path,
                "createCar",
                Arrays.asList(
                        new Parameter("id", "string", id),
                        new Parameter("make", "string", "Mercedes"),
                        new Parameter("model", "string", "clk"),
                        new Parameter("colour", "string", "Black"),
                        new Parameter("owner", "string", "Ghareeb")),
                Collections.emptyList(),
                1.0).get();

        Transaction result = this.adapter.invokeSmartContract(
                path,
                "queryAllCars",
                new ArrayList<>(),
                new ArrayList<>(),
                1.0).get();
        String value = result.getReturnValues().get(0).getValue();
        log.debug("Looking for Id: " + id);
        Assertions.assertTrue(value.contains(id));
    }
}