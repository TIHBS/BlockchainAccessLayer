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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.utils.ScipParser;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.model.SmartContractFunctionArgument;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import io.reactivex.Observable;
import org.apache.http.MethodNotSupportedException;
import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FabricAdapter implements BlockchainAdapter {
    @Override
    public CompletableFuture<Transaction> submitTransaction(long waitFor, String receiverAddress, BigDecimal value) throws InvalidTransactionException, MethodNotSupportedException {
        throw new MethodNotSupportedException("Fabric does not support submitting monetary transactions!");
    }

    @Override
    public Observable<Transaction> receiveTransactions(long waitFor, String senderId) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Fabric does not support receiving monetary transactions!");
    }

    @Override
    public CompletableFuture<TransactionState> ensureTransactionState(long waitFor, String transactionId) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Fabric does not support monetary transactions!");
    }

    @Override
    public CompletableFuture<TransactionState> detectOrphanedTransaction(String transactionId) throws MethodNotSupportedException {
        throw new MethodNotSupportedException("Fabric does not support monetary transactions!");
    }

    @Override
    public CompletableFuture<Transaction> invokeSmartContract(String functionIdentifier, List<SmartContractFunctionArgument> parameters, double requiredConfidence) throws MethodNotSupportedException {
        ScipParser parser = ScipParser.parse(functionIdentifier);

        HFClient client = HFClient.createNewInstance();
        Channel channel = client.getChannel(parser.getFunctionPathSegments()[0]);
        ///////////////
        /// Send transaction proposal to all peers
        TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
        transactionProposalRequest.setChaincodeID(ChaincodeID.newBuilder().build());
        // todo add language type to gateway config
        transactionProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
        //transactionProposalRequest.setFcn("invoke");
        transactionProposalRequest.setFcn(parser.getFunctionName());
        transactionProposalRequest.setProposalWaitTime(0);
        transactionProposalRequest.setArgs(parameters.stream().map(SmartContractFunctionArgument::getValue).collect(Collectors.joining()));

        Map<String, byte[]> tm2 = new HashMap<>();
        tm2.put("result", ":)".getBytes(UTF_8));  //
        Transaction result = null;
        try {
            transactionProposalRequest.setTransientMap(tm2);
            ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
            chaincodeEndorsementPolicy.fromYamlFile(new File("/sdkintegration/chaincodeendorsementpolicy.yaml"));
            transactionProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

            //  Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposalToEndorsers(transactionProposalRequest);
            Collection<ProposalResponse> transactionPropResp = channel.sendTransactionProposal(transactionProposalRequest, channel.getPeers());
            for (ProposalResponse response : transactionPropResp) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    result = new Transaction();
                    result.setState(TransactionState.CONFIRMED);
                    result.setTransactionHash(response.getTransactionID());
                }
            }
        } catch (InvalidArgumentException | ChaincodeEndorsementPolicyParseException | IOException | ProposalException e) {
            e.printStackTrace();
            result = new Transaction();
            result.setState(TransactionState.INVALID);
        }

        return CompletableFuture.completedFuture(result);
    }
}
