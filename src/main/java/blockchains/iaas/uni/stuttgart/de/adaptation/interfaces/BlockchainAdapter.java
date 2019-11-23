/********************************************************************************
 * Copyright (c) 2018-2019 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package blockchains.iaas.uni.stuttgart.de.adaptation.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.exceptions.ParameterException;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import io.reactivex.Observable;

public interface BlockchainAdapter {
    /**
     * submits a transaction to the blockchain that transfers an amount of the native crypto-currency to some address.
     *
     * @param requiredConfidence the degree-of-confidence required to be achieved before sending a callback message to the invoker.
     * @param receiverAddress    the address of the receiver
     * @param value              the value to transfer measured in the most granular unit, e.g., wei, satoshi
     * @return a completable future that emits a summary of the submitted transaction.
     * The future should normally complete with a transaction of the state CONFIRMED if the desired number of block-confirmations were received,
     * and with a transaction of the state NOT_FOUND if the transaction was committed to a block and then orphaned and invalidated.
     * The future should exceptionally complete with an exception of type BlockchainNodeUnreachableException if the blockchain node is not reachable,
     * and with an exception of type InvalidTransactionException if the transaction is initially invalid (e.g., malformed)
     * @throws InvalidTransactionException if the submitted transaction causes an immediate validation error, e.g.,
     *                                     insufficient funds, or incorrect receiverAddress (this seems to never be thrown)
     */
    CompletableFuture<Transaction> submitTransaction(String receiverAddress, BigDecimal value, double requiredConfidence) throws InvalidTransactionException, NotSupportedException;

    /**
     * receives transactions addressed to us (potentially from a specific sender)
     *
     * @param requiredConfidence the degree-of-confidence required to be achieved before sending a callback message to the invoker.
     * @param senderId           an optional address of the sender. If specified, only transactions from this sender are considered
     * @return an observable that emits a summary of the received transaction whenever one is detected
     */
    Observable<Transaction> receiveTransactions(String senderId, double requiredConfidence) throws NotSupportedException;

    /**
     * ensures that a transaction receives enough block-confirmations
     *
     * @param requiredConfidence the degree-of-confidence required to be achieved before sending a callback message to the invoker.
     * @param transactionId      the hash of the transaction we want to monitor
     * @return a completable future that emits the new state of the transaction (either COFIRMED in case the desired
     * number of block-confirmations got received, or NOT_FOUND if the transaction got invalidated).
     * The future should exceptionally complete with an exception of type BlockchainNodeUnreachableException if the blockchain node is not reachable
     */
    CompletableFuture<TransactionState> ensureTransactionState(String transactionId, double requiredConfidence) throws NotSupportedException;

    /**
     * detects that the given transaction got orphaned
     *
     * @param transactionId the hash of the transaction we want to monitor
     * @return a completable future that emits the new state of the transaction (PENDING meaning that it no longer has a
     * block, i.e., it is orphaned)
     * The future should exceptionally complete with an exception of type BlockchainNodeUnreachableException if the blockchain node is not reachable
     */
    CompletableFuture<TransactionState> detectOrphanedTransaction(String transactionId) throws NotSupportedException;

    /**
     * invokes a smart contract function
     *
     * @param smartContractPath  the path to the smart contract
     * @param functionIdentifier the function name
     * @param inputs             the input parameters of the function to be invoked
     * @param outputs            the output parameters of the function to be invoked
     * @param requiredConfidence the degree-of-confidence required to be achieved before sending a callback message to the invoker.
     * @return a completable future that emits a new transaction object holding the result of the invocation.
     * @throws NotSupportedException if the underlying blockchain system does not support smart contracts.
     */
    CompletableFuture<Transaction> invokeSmartContract(
            String smartContractPath,
            String functionIdentifier,
            List<Parameter> inputs,
            List<Parameter> outputs,
            double requiredConfidence
    ) throws NotSupportedException, ParameterException;

    /**
     * Tests the connection settings with the underlying blockchain
     *
     * @return true if the connection is successful, false otherwise.
     */
    boolean testConnection();
}
