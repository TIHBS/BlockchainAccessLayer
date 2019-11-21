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

package blockchains.iaas.uni.stuttgart.de.adaptation.adapters.ethereum;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.contracts.Permissions;
import blockchains.iaas.uni.stuttgart.de.model.LinearChainTransaction;
import blockchains.iaas.uni.stuttgart.de.model.SmartContractFunctionArgument;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import org.web3j.tx.gas.DefaultGasProvider;

/**
 * To run these tests, you need ganache with the following mnemonic:
 * smart contract composition
 */
class EthereumAdapterTest {
    private static final String NETWORK_NAME = "eth-0";
    private static final String MESSAGE = "This was not a difficult task!";
    private static final String SENDER_ADDRESS = "0x90645Dc507225d61cB81cF83e7470F5a6AA1215A";
    private static final double REQUIRED_CONFIDENCE = 0.9;
    private EthereumAdapter adapter;
    private static final Logger log = LoggerFactory.getLogger(EthereumAdapterTest.class);

    @BeforeAll
    static void initAll() throws URISyntaxException {
        final String DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME = "gatewayConfiguration.json";
        final File file = new File(Objects.requireNonNull(
                EthereumAdapterTest.class.getClassLoader().getResource(DEFAULT_CONNECTION_PROFILES_CONFIGURATION_FILE_NAME)).toURI());
        ConnectionProfilesManager manager = ConnectionProfilesManager.getInstance();
        manager.resetConnectionProfiles();
        manager.loadConnectionProfilesFromFile(file);
    }
    @BeforeEach
    void init() {
        this.adapter = (EthereumAdapter) AdapterManager.getInstance().getAdapter(NETWORK_NAME);
    }

    @Test
    void testConnectionToNode() {
        Assertions.assertTrue(this.adapter.testConnectionToNode());
    }

    @Test
    void testSendTransaction() throws ExecutionException, InterruptedException {
        final String toAddress = "0x182761AC584C0016Cdb3f5c59e0242EF9834fef0";
        final BigDecimal value = new BigDecimal(5000);
        LinearChainTransaction result = (LinearChainTransaction) this.adapter.submitTransaction(toAddress, value, REQUIRED_CONFIDENCE).get();
        log.debug("transaction hash is: " + result.getTransactionHash());
    }

    @Test
    void testInvokeSmartContract() throws Exception {
        Permissions contract = this.deployContract();
        String scip = String.format("scip://%s/%s/setPublicKey?publicKey=bytes:void", NETWORK_NAME, contract.getContractAddress());
        byte[] bytes = MESSAGE.getBytes();
        String argument = new BigInteger(bytes).toString(16);
        List<SmartContractFunctionArgument> argumentList = Collections.singletonList(new SmartContractFunctionArgument("publicKey", argument));
        this.adapter.invokeSmartContract(scip, argumentList, REQUIRED_CONFIDENCE).get();

//        byte[] result = contract.getPublicKey("0x90645Dc507225d61cB81cF83e7470F5a6AA1215A").send();
//        log.debug(new String(result));
        scip = String.format("scip://%s/%s/getPublicKey?ethereumAddress=address:bytes", NETWORK_NAME, contract.getContractAddress());
        //String scip = "scip://eth-0/0x14E8548A45551d4a052884d68bd3F924AE13c7F4/getPublicKey?ethereumAddress=address:bytes";
        Transaction result =  this.adapter.invokeSmartContract(scip,
                Collections.singletonList(new SmartContractFunctionArgument("ethereumAddress", "0x90645Dc507225d61cB81cF83e7470F5a6AA1215A")), REQUIRED_CONFIDENCE).get();
        String value = result.getReturnValue();
        log.debug(value);
    }

    @Test
    @Disabled
    void createNewKeystoreFile() throws CipherException, IOException {
        final String filePath = "C:\\Ethereum\\keystore";
        final File file = new File(filePath);
        final String password = "123456789";
        final String privateKey = "6871412854632d2ccd9c99901f5a0a3d838b31dbc6bfecae5f2382d6b7658bbf";
        ECKeyPair pair = ECKeyPair.create(new BigInteger(privateKey, 16));
        WalletUtils.generateWalletFile(password, pair, file, false);
    }

    @Test
    @Disabled
    void testDepoloyContract() throws InterruptedException, ExecutionException, IOException {
        Permissions permissions = this.deployContract();
        log.debug("Deployed smart contract. Address: {}", permissions.getContractAddress());
    }

    Permissions deployContract() throws ExecutionException, InterruptedException, IOException {
        Permissions contract = Permissions.deploy(this.adapter.getWeb3j(), this.adapter.getCredentials(),
                new DefaultGasProvider()).sendAsync().get();
        Assertions.assertTrue(contract.isValid());

        return contract;
    }
}