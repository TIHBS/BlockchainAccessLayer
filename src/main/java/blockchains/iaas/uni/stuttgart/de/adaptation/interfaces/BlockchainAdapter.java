package blockchains.iaas.uni.stuttgart.de.adaptation.interfaces;

import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.exceptions.SubmitTransactionException;
import rx.Observable;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/********************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 ********************************************************************************/
public interface BlockchainAdapter {
    /**
     * submits a transaction to the blockchain that transfers an amount of the native crypto-currency to some address.
     * @param waitFor the number of block-confirmations to receive before emitting a result
     * @param timeoutMillis the number of milliseconds to wait before considering the transaction TIMED_OUT,
     *                      or -1 if no timeout period is to be specified.
     * @param receiverAddress the address of the receiver
     * @param value the value to transfer measured in the most granular unit, e.g., wei, satochi
     * @return a completable future that emits a summary of the submitted transaction.
     * @throws SubmitTransactionException if the submitted transaction causes an immediate validation error, e.g.,
     * insufficient funds, or incorrect receiverAddress
     */
    CompletableFuture<Transaction> submitTransaction(long waitFor, long timeoutMillis, String receiverAddress, BigDecimal value) throws SubmitTransactionException;

    /**
     * subscribes to the event of receiving a new transaction
     * @param waitFor the number of block-confirmations to be detected before emitting a result
     * @param senderId an optional address of the sender. If specified, only transactions from this sender are considered
     * @return an completable future that emits a summary of the received transaction
     */
    Observable<Transaction> subscribeToReceivedTransactions(int waitFor, Optional<String> senderId);


    /**
     * subscribes to the event of a transaction changing its state
     * @param waitFor the number of block-confirmations to be detected before considering the transaction to be confirmed
     *                * @param timeoutMillis the number of milliseconds to wait before considering the transaction TIMED_OUT,
     *                      or -1 if no timeout period is to be specified.
     * @param transactionHash the hash of the transaction we want to monitor
     * @return an completable future that emits the new state of the transaction
     */
    CompletableFuture<TransactionState> subscribeToTransactionState(int waitFor, int timeoutMillis, String transactionHash);

    /**
     * Indicates whether the specified transaction is recognized by the blockchain
     * @param transactionHash the hash of the transaction to consider
     * @return <code>true</code> if the transaction is recognized by the corresponding blockchain node; otherwise \
     * <code>false</code>
     */
    boolean doesTransactionExist(String transactionHash) throws IOException;
}
