package blockchains.iaas.uni.stuttgart.de.adaptation.interfaces;

import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import rx.Observable;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

/********************************************************************************
 * Copyright (c) 2018 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
public interface BlockchainAdapter {
    /**
     * submits a transaction to the blockchain that transfers an amount of the native crypto-currency to some address.
     * @param waitFor the number of block-confirmations to receive before emitting a result
     * @param receiverAddress the address of the receiver
     * @param value the value to transfer measured in the most granular unit, e.g., wei, satochi
     * @return a completable future that emits a summary of the submitted transaction.
     * The future should exceptionally complete with an exception of type BlockchainNodeUnreachableException if the blockchain node is not reachable,
     * and with an exception of type InvalidTransactionException if the transaction is invalid (e.g., malformed)
     * @throws InvalidTransactionException if the submitted transaction causes an immediate validation error, e.g.,
     * insufficient funds, or incorrect receiverAddress (this seems to never be thrown)
     */
    CompletableFuture<Transaction> submitTransaction(long waitFor, String receiverAddress, BigDecimal value) throws InvalidTransactionException;

    /**
     * receives transactions addressed to us (potentially from a specific sender)
     * @param waitFor the number of block-confirmations to be detected before emitting a result
     * @param senderId an optional address of the sender. If specified, only transactions from this sender are considered
     * @return an observable that emits a summary of the received transaction whenever one is detected
     */
    Observable<Transaction> receiveTransactions(long waitFor, String senderId);


    /**
     * ensures that a transaction receives enough block-confirmations
     * @param waitFor the number of block-confirmations to be detected before considering the transaction to be confirmed
     * @param transactionId the hash of the transaction we want to monitor
     * @return a completable future that emits the new state of the transaction (either COFIRMED in case the desired
     * number of block-confirmations got received, or NOT_FOUND if the transaction got invalidated).
     * The future should exceptionally complete with an exception of type BlockchainNodeUnreachableException if the blockchain node is not reachable
     */
    CompletableFuture<TransactionState> ensureTransactionState(long waitFor, String transactionId);


    /**
     * detects that the given transaction got orphaned
     * @param transactionId the hash of the transaction we want to monitor
     * @return a completable future that emits the new state of the transaction (PENDING meaning that it no longer has a
     * block, i.e., it is orphaned)
     * The future should exceptionally complete with an exception of type BlockchainNodeUnreachableException if the blockchain node is not reachable
     */
    CompletableFuture<TransactionState> detectOrphanedTransaction(String transactionId);

    /**
     * Indicates whether the specified transaction is recognized by the blockchain
     * @param transactionId the hash of the transaction to look for
     * @return <code>true</code> if the transaction is recognized by the corresponding blockchain node; otherwise \
     * <code>false</code>
     */
    boolean doesTransactionExist(String transactionId) throws IOException;
}
