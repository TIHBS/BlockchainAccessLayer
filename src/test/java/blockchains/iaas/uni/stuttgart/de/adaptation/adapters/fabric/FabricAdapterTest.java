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

import java.util.concurrent.ExecutionException;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo fix test!
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
        adapter = (FabricAdapter) AdapterManager.getInstance().getAdapter(NETWORK_NAME);
    }

    @Test
    public void testRoundTrip() throws ExecutionException, InterruptedException {
        String scip = String.format("scip://%s/%s/%s/createCar?id=string&make=string&model=string&colour=string&owner=string:void", NETWORK_NAME, CHANNEL_NAME, CHAINCODE_NAME);
        final String id = String.format("CAR%s", RandomStringUtils.randomNumeric(6));
//        this.adapter.invokeSmartContract(scip,
//                Arrays.asList(
//                        new SmartContractFunctionArgument("id", id),
//                        new SmartContractFunctionArgument("make", "Mercedes"),
//                        new SmartContractFunctionArgument("model", "clk"),
//                        new SmartContractFunctionArgument("colour", "Black"),
//                        new SmartContractFunctionArgument("owner", "Ghareeb"))
//                , 1.0).get();
        scip = String.format("scip://%s/%s/%s/queryAllCars?:String", NETWORK_NAME, CHANNEL_NAME, CHAINCODE_NAME);
//        Transaction result = this.adapter.invokeSmartContract(scip, new ArrayList<>(), 1.0).get();
//        String value = result.getReturnValue();
//        log.debug("Looking for Id: " + id);
//        Assertions.assertTrue(value.contains(id));
    }
}