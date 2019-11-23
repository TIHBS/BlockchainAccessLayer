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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.contracts.Permissions;
import blockchains.iaas.uni.stuttgart.de.model.LinearChainTransaction;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
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

// todo fix test

/**
 * To run these tests, you need ganache with the following mnemonic:
 * smart contract composition
 */
class EthereumAdapterTest {
    private static final String NETWORK_NAME = "eth-0";
    private static final String MESSAGE = "This was not a difficult task!";
    private final String BYTES_TYPE = "{\n" +
            "\t\"type\": \"array\",\n" +
            "\t\"items\": {\n" +
            "\t\t\"type\": \"string\",\n" +
            "\t\t\"pattern\": \"^[a-fA-F0-9]{2}$\"\n" +
            "\t}\n" +
            "}";
    private final String ADDRESS_TYPE = "{\n" +
            "\t\"type\": \"string\",\n" +
            "\t\"pattern\": \"^0x[a-fA-F0-9]{40}$\"\n" +
            "}";
    private static final double REQUIRED_CONFIDENCE = 0.6;
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
        String smartContractPath = contract.getContractAddress();
        String functionIdentifier = "setPublicKey";
        byte[] bytes = MESSAGE.getBytes();
        String argument = new BigInteger(bytes).toString(16);
        List<Parameter> inputs = Collections.singletonList(new Parameter("publicKey", BYTES_TYPE, argument));
        List<Parameter> outputs = Collections.emptyList();
        LinearChainTransaction init = (LinearChainTransaction) this.adapter.invokeSmartContract(smartContractPath, functionIdentifier, inputs, outputs, REQUIRED_CONFIDENCE).get();
        log.info("initial transaction {}", init.getTransactionHash());
        functionIdentifier = "getPublicKey";
        inputs = Collections.singletonList(new Parameter("ethereumAddress", ADDRESS_TYPE, "0x90645Dc507225d61cB81cF83e7470F5a6AA1215A"));
        outputs = Collections.singletonList(new Parameter("return", BYTES_TYPE, null));
        Transaction result = this.adapter.invokeSmartContract(smartContractPath, functionIdentifier, inputs, outputs, REQUIRED_CONFIDENCE).get();
        String value = result.getReturnValues().get(0);
        log.debug(value);
        String retrievedMessage = new String(new BigInteger(value, 16).toByteArray(), StandardCharsets.UTF_8);
        Assertions.assertEquals(MESSAGE, retrievedMessage);
        log.debug(retrievedMessage);
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

    Permissions deployContract() throws ExecutionException, InterruptedException, IOException {
        Permissions contract = Permissions.deploy(this.adapter.getWeb3j(), this.adapter.getCredentials(),
                new DefaultGasProvider()).sendAsync().get();
        Assertions.assertTrue(contract.isValid());

        return contract;
    }
}