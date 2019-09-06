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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.model.SmartContractFunctionArgument;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void testInvokeSCFunction() throws ExecutionException, InterruptedException {
        String scip = String.format("scip://%s/%s/%s/createCar?id=string&make=string&model=string&colour=string&owner=string:void", NETWORK_NAME, CHANNEL_NAME, CHAINCODE_NAME);
        Transaction result = this.adapter.invokeSmartContract(scip,
                Arrays.asList(
                        new SmartContractFunctionArgument("id", "CAR-gbf"),
                        new SmartContractFunctionArgument("make", "Mercedes"),
                        new SmartContractFunctionArgument("model", "clk"),
                        new SmartContractFunctionArgument("colour", "Black"),
                        new SmartContractFunctionArgument("owner", "Ghareeb"))
                , 1.0).get();
        String value = result.getReturnValue();
        log.debug(value);
    }
}