package blockchains.iaas.uni.stuttgart.de.adaptation.adapters;

import blockchains.iaas.uni.stuttgart.de.exceptions.SubmitTransactionException;
import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.model.Block;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
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
public class EthereumAdapter implements BlockchainAdapter {
    private Credentials credentials;
    private final String nodeUrl;
    private final Web3j web3j;
    private static final Logger log = LoggerFactory.getLogger(EthereumAdapter.class);


    public EthereumAdapter(final String nodeUrl) {
        this.nodeUrl = nodeUrl;
        this.web3j = Web3j.build(new HttpService(this.nodeUrl));
    }


    public boolean testConnectionToNode() {
        try {
            log.info("Connected to Ethereum client: URL: {}, Version: {}", this.nodeUrl, this.web3j.web3ClientVersion().send().getWeb3ClientVersion());
            return true;
        } catch (IOException e) {
            log.error("Failed to connect to Ethereum client at URL: {}. Reason: {}", this.nodeUrl, e.getMessage());

            return false;
        }
    }


    public void setCredentials(String password, String fileSource) throws IOException, CipherException {
        try {
            this.credentials = WalletUtils.loadCredentials(password, fileSource);
        } catch (IOException | CipherException e) {
            log.error("Error occurred while setting the user credentials for Ethereum. Reason {}", e.getMessage());
            throw e;
        }
    }

    private CompletableFuture<Transaction> subscribeForTxEvent(String txHash, long waitFor, long timeoutMillis, TransactionState... observedStates) {
        final CompletableFuture<Transaction> result = new CompletableFuture<>();
        final Subscription subscription = web3j.blockObservable(false).subscribe(new Subscriber<EthBlock>() {
            final Instant start = Instant.now();
            Instant now;

            void handleDetectedState(final Optional<org.web3j.protocol.core.methods.response.Transaction> transactionDetails,
                                     final TransactionState detectedState, final TransactionState[] interesting,
                                     CompletableFuture<Transaction> future) {
                // Only complete the future if we are interested in this event
                if (Arrays.asList(interesting).contains(detectedState)) {
                    final Transaction result = new Transaction();
                    result.setState(detectedState);

                    if (transactionDetails.isPresent()) {
                        result.setBlock(new Block(transactionDetails.get().getBlockNumber(), transactionDetails.get().getBlockHash()));
                        result.setFrom(transactionDetails.get().getFrom());
                        result.setTo(transactionDetails.get().getTo());
                        result.setTransactionHash(transactionDetails.get().getHash());
                        result.setValue(transactionDetails.get().getValue());
                    }
                    future.complete(result);

                }

            }

            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {
                result.completeExceptionally(throwable);
            }

            @Override
            public void onNext(EthBlock ethBlock) {
                try {
                    // make sure the transaction exists
                    final EthTransaction transaction = web3j.ethGetTransactionByHash(txHash).send();

                    if (!transaction.getTransaction().isPresent()) {
                        final String msg = String.format("The transaction of the hash %s is not found!", txHash);
                        log.info(msg);
                        handleDetectedState(transaction.getTransaction(), TransactionState.NOT_FOUND, observedStates, result);

                        return;

                    }

                    // make sure the transaction is still contained in a block, i.e., it was not orphaned
                    final String retrievedBlockHash = transaction.getTransaction().get().getBlockHash();

                    if (retrievedBlockHash == null || retrievedBlockHash.isEmpty()) {
                        final String msg = String.format("The transaction of the hash %s has no block (orphaned?)",
                                txHash);
                        log.info(msg);

                        handleDetectedState(transaction.getTransaction(), TransactionState.PENDING, observedStates, result);
                        return;

                    }
                    // check if enough block-confirmations have occurred.
                    if (ethBlock.getBlock() != null) {
                        if (ethBlock.getBlock().getNumber()
                                .subtract(transaction.getTransaction().get().getBlockNumber())
                                .intValue() >= waitFor) {
                            final String msg = String.format("The transaction of the hash %s has been confirmed",
                                    txHash);
                            log.info(msg);

                            handleDetectedState(transaction.getTransaction(), TransactionState.CONFIRMED, observedStates, result);
                            return;
                        }
                    }

                    // check if timeout occurred
                    now = Instant.now();

                    if (timeoutMillis >= 0 && Duration.between(start, now).toMillis() > timeoutMillis) {
                        final String msg = String.format("The transaction of the hash %s has timed-out",
                                txHash);
                        log.info(msg);

                        handleDetectedState(transaction.getTransaction(), TransactionState.TIMED_OUT, observedStates, result);
                    }


                } catch (IOException e) {
                    result.completeExceptionally(e);
                }
            }
        });
        //unsubscribe the observable when the CompletableFuture completes (either when detecting an event, or manually)
        result.whenComplete((v, e) -> subscription.unsubscribe());

        return result;
    }


    @Override
    public CompletableFuture<Transaction> submitTransaction(long waitFor, long timeoutMillis, String receiverAddress, BigDecimal value)
            throws SubmitTransactionException {
        if (credentials == null) {
            log.error("Credentials are not set for the Ethereum user");
            throw new NullPointerException("Credentials are not set for the Ethereum user");
        }

        try {
            return Transfer.sendFunds(web3j, credentials, receiverAddress, value, Convert.Unit.WEI)  // 1 wei = 10^-18 Ether
                    .sendAsync().exceptionally((e) -> {
                        log.error("Failed to submitTransaction. Reason: {}", e.getMessage());
                        return null;
                    })
                    .thenCompose(tx -> {
                        if (tx == null)
                            return null;
                        return subscribeForTxEvent(tx.getTransactionHash(), waitFor, timeoutMillis, TransactionState.CONFIRMED,
                                TransactionState.TIMED_OUT);
                    });

        } catch (Exception e) {
            final String msg = "An error occurred while trying to submit a new transaction. Reason: " + e.getMessage();
            log.error(msg);

            throw new SubmitTransactionException(msg, e);
        }


    }

    @Override
    public Observable<Transaction> subscribeToReceivedTransactions(int waitFor, Optional<String> senderId) {
        if (credentials == null) {
            log.error("Credentials are not set for the Ethereum user");
            throw new NullPointerException("Credentials are not set for the Ethereum user");
        }

        final String myAddress = credentials.getAddress();
        final PublishSubject<Transaction> result = PublishSubject.create();
        final Subscription newTransactionObservable = web3j.transactionObservable().subscribe(tx -> {
            if (myAddress.equalsIgnoreCase(tx.getTo())) {
                if (!senderId.isPresent() || (senderId.isPresent() && senderId.get().equalsIgnoreCase(tx.getFrom()))) {
                    log.info("New transaction received from {}", tx.getFrom());
                    subscribeForTxEvent(tx.getHash(), waitFor, -1, TransactionState.CONFIRMED)
                            .thenAccept(result::onNext);
                }
            }
        });


        result.doOnUnsubscribe(newTransactionObservable::unsubscribe);

        return result;

    }

    @Override
    public CompletableFuture<TransactionState> subscribeToTransactionState(int waitFor, int timeoutMillis, String transactionHash) {
        return subscribeForTxEvent(transactionHash, waitFor, timeoutMillis, TransactionState.values())
                .thenApply(Transaction::getState);
    }

    @Override
    public boolean doesTransactionExist(String transactionHash) throws IOException {
        final EthTransaction transaction = web3j.ethGetTransactionByHash(transactionHash).send();

        return transaction.getTransaction().isPresent();
    }

}
