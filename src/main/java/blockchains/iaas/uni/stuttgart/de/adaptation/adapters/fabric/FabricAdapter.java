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

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.utils.SmartContractPathParser;
import blockchains.iaas.uni.stuttgart.de.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvokeSmartContractFunctionFailure;
import blockchains.iaas.uni.stuttgart.de.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.exceptions.ParameterException;
import blockchains.iaas.uni.stuttgart.de.model.Occurrence;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import io.reactivex.Observable;
import lombok.Builder;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Builder
public class FabricAdapter implements BlockchainAdapter {
    private String walletPath;
    private String userName;
    private String connectionProfilePath;
    private static final Logger log = LoggerFactory.getLogger(FabricAdapter.class);

    public String getWalletPath() {
        return walletPath;
    }

    public void setWalletPath(String walletPath) {
        this.walletPath = walletPath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getConnectionProfilePath() {
        return connectionProfilePath;
    }

    public void setConnectionProfilePath(String connectionProfilePath) {
        this.connectionProfilePath = connectionProfilePath;
    }

    @Override
    public CompletableFuture<Transaction> submitTransaction(String receiverAddress, BigDecimal value, double requiredConfidence

    ) throws InvalidTransactionException, NotSupportedException {
        throw new NotSupportedException("Fabric does not support submitting monetary transactions!");
    }

    @Override
    public Observable<Transaction> receiveTransactions(String senderId, double requiredConfidence) throws NotSupportedException {
        throw new NotSupportedException("Fabric does not support receiving monetary transactions!");
    }

    @Override
    public CompletableFuture<TransactionState> ensureTransactionState(String transactionId, double requiredConfidence) throws NotSupportedException {
        throw new NotSupportedException("Fabric does not support monetary transactions!");
    }

    @Override
    public CompletableFuture<TransactionState> detectOrphanedTransaction(String transactionId) throws NotSupportedException {
        throw new NotSupportedException("Fabric does not support monetary transactions!");
    }

    @Override
    public CompletableFuture<Transaction> invokeSmartContract(String smartContractPath, String functionIdentifier, List<Parameter> inputs, List<Parameter> outputs, double requiredConfidence) throws NotSupportedException, BalException {
        SmartContractPathParser parser = SmartContractPathParser.parse(smartContractPath);
        String[] pathSegments = parser.getSmartContractPathSegments();
        String channelName;
        String chaincodeName;
        String smartContractName = null;

        if (outputs.size() > 1) {
            throw new ParameterException("Hyperledger Fabric supports only at most a single return value.");
        }

        if (pathSegments.length != 3 && pathSegments.length != 2) {
            String message = String.format("Unable to identify the path to the requested function. Expected path segements: 3 or 2. Found path segments: %s", pathSegments.length);
            log.error(message);
            throw new InvokeSmartContractFunctionFailure(message);
        } else {
            channelName = pathSegments[0];
            chaincodeName = pathSegments[1];

            if (pathSegments.length == 3) {
                smartContractName = pathSegments[2];
            }

            try {
                Gateway.Builder builder = this.getGatewayBuilder();

                // Create a gateway connection
                try (Gateway gateway = builder.connect()) {
                    // Obtain a smart contract deployed on the network.
                    Network network = gateway.getNetwork(channelName);
                    Contract contract;

                    if (smartContractName != null) {
                        contract = network.getContract(chaincodeName, smartContractName);
                    } else {
                        contract = network.getContract(chaincodeName);
                    }

                    String[] params = inputs.stream().map(Parameter::getValue).toArray(String[]::new);
                    byte[] resultAsBytes = contract.submitTransaction(functionIdentifier, params);

                    CompletableFuture<Transaction> result = new CompletableFuture<>();
                    Transaction resultT = new Transaction();

                    if (outputs.size() == 1) {
                        Parameter resultP = Parameter
                                .builder()
                                .name(outputs.get(0).getName())
                                .value(new String(resultAsBytes, StandardCharsets.UTF_8))
                                .build();
                        resultT.setReturnValues(Collections.singletonList(resultP));
                        log.info(resultP.getValue());
                    } else if (outputs.size() == 0) {
                        log.info("Fabric transaction without a return value executed!");
                        resultT.setReturnValues(Collections.emptyList());
                    }

                    resultT.setState(TransactionState.RETURN_VALUE);
                    result.complete(resultT);
                    return result;
                }
            } catch (IOException | ContractException | TimeoutException | InterruptedException e) {
                throw new InvokeSmartContractFunctionFailure(e.getMessage());
            }
        }
    }

    @Override
    public Observable<Occurrence> subscribeToEvent(String smartContractAddress, String eventIdentifier, List<Parameter> outputParameters, double degreeOfConfidence, String filter) throws BalException {
        return null;
    }

    private Gateway.Builder getGatewayBuilder() throws IOException {
        // Load an existing wallet holding identities used to access the network.
        Path walletDirectory = Paths.get(walletPath);

        Wallet wallet = Wallet.createFileSystemWallet(walletDirectory);

        // Path to a connection profile describing the network.
        Path networkConfigFile = Paths.get(this.connectionProfilePath);

        // Configure the gateway connection used to access the network.
        return Gateway.createBuilder()
                .identity(wallet, userName)
                .networkConfig(networkConfigFile);
    }

    @Override
    public boolean testConnection() {
        try (Gateway gateway = this.getGatewayBuilder().connect()) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
