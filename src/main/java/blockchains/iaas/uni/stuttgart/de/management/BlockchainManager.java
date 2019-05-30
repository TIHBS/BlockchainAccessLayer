package blockchains.iaas.uni.stuttgart.de.management;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainIdNotFoundException;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainNodeUnreachableException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.management.callback.CallbackManager;
import blockchains.iaas.uni.stuttgart.de.management.callback.MessageTranslatorFactory;
import blockchains.iaas.uni.stuttgart.de.management.model.CompletableFutureSubscription;
import blockchains.iaas.uni.stuttgart.de.management.model.ObservableSubscription;
import blockchains.iaas.uni.stuttgart.de.management.model.Subscription;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/********************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
public class BlockchainManager {
    private static final Logger log = LoggerFactory.getLogger(BlockchainManager.class);

    /**
     * Submits a transaction to the blockchain, and sends a callback message informing a remote endpoint of the result.
     * The status of the result could be:
     * <p>
     * UNKNOWN: the blockchain network is not recognized, or connection to node is not possible.
     * INVALID: the submitted transaction faild validation at the node
     * CONFIRMED (along with the tx itself): the submitted transaction received the desired number of block-confirmations
     *
     * @param subscriptionId supplied by the remote application as a means for correlation
     * @param to             the address of the transaction recipient
     * @param value          the value of the transaction
     * @param blockchainId   the blockchain network id
     * @param waitFor        the number of block-confirmations to wait for until determining the transaction is durably persisted
     * @param epUrl          the url of the endpoint to send the callback message to
     */
    public void submitNewTransaction(final String subscriptionId, final String to, final BigInteger value,
                                     final String blockchainId, final long waitFor, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();

        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<Transaction> future = adapter.submitTransaction(waitFor, to, new BigDecimal(value));
            // This happens when a communication error, or an error with the tx exist.
            future.
                    thenAccept(tx -> {
                        if (tx != null) {
                            if (tx.getState() == TransactionState.CONFIRMED) {
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, false, tx));
                            } else {// it is NOT_FOUND
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, tx));
                            }
                        } else
                            log.info("resulting transaction is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to submit a transaction. Reason: {}", e.getMessage());
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, TransactionState.UNKNOWN));
                        else if (e.getCause() instanceof InvalidTransactionException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, TransactionState.INVALID));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(subscriptionId);
                    });

            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.SUBMIT_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(subscriptionId, subscription);
        } catch (InvalidTransactionException e) {
            // This (should only) happen when something is wrong with the transaction data
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, TransactionState.INVALID));
        } catch (BlockchainIdNotFoundException e) {
            // This (should only) happen when the blockchainId is not found
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, TransactionState.UNKNOWN));
        }
    }

    /**
     * Receives the stream of all transactions addressed to us on the specified blockchainId, and for each one, sends a
     * callback message to the remote endpoint. A callback message is only sent for transactions that have enough block-
     * confirmations. No callback messages are sent for erroneous cases.
     * The status of the result is always:
     * <p>
     * CONFIRMED
     *
     * @param subscriptionId supplied by the remote application as a means for correlation
     * @param from           an optional parameter.If supplied, it indicates the sending address of the transactions we are interested
     *                       in.
     * @param blockchainId   the blockchain network id
     * @param waitFor        the number of block-confirmations to wait for until determining the transaction is durably persisted
     * @param epUrl          the url of the endpoint to send the callback message to
     */
    public void receiveTransactions(final String subscriptionId, final String from, final String blockchainId,
                                    final long waitFor, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final Disposable subscription = adapter.receiveTransactions(waitFor, from)
                    .doFinally(() -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(subscriptionId);
                    })
                    .doOnError(throwable -> log.error("Failed to receive transaction. Reason:{}", throwable.getMessage()))
                    .subscribe(transaction -> {
                        if (transaction != null) {
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, false, transaction));
                        } else {
                            log.error("received transaction is null!");
                        }
                    });

            // Add subscription to the list of subscriptions
            final Subscription sub = new ObservableSubscription(subscription, SubscriptionType.RECEIVE_TRANSACTIONS);
            SubscriptionManager.getInstance().createSubscription(subscriptionId, sub);
        } catch (BlockchainIdNotFoundException e) {
            // This (should only) happen when the blockchainId is not found
            log.error("blockchainId ({}) is not recognized, but no error callback is sent to endpoint!", blockchainId);
        }
    }

    /**
     * Receives a single transaction addressed to us on the specified blockchainId, and sends a
     * callback message to the remote endpoint. A callback message is only sent for transactions that have enough block-
     * confirmations.
     * The status of the result could be:
     * <p>
     * UNKNOWN: the blockchain network is not recognized, or connection to node is not possible.
     * CONFIRMED: the submitted transaction received the desired number of block-confirmations
     *
     * @param subscriptionId supplied by the remote application as a means for correlation
     * @param from           an optional parameter.If supplied, it indicates the sending address of the transactions we are interested
     *                       in.
     * @param blockchainId   the blockchain network id
     * @param waitFor        the number of block-confirmations to wait for until determining the transaction is durably persisted
     * @param epUrl          the url of the endpoint to send the callback message to
     */
    public void receiveTransaction(final String subscriptionId, final String from, final String blockchainId,
                                   final long waitFor, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final Disposable subscription = adapter.receiveTransactions(waitFor, from)
                    .doFinally(() -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(subscriptionId);
                    })
                    .doOnError(throwable -> {
                        log.error("Failed to receive transaction. Reason: " + throwable.getMessage());

                        if (throwable instanceof BlockchainNodeUnreachableException || throwable.getCause() instanceof BlockchainNodeUnreachableException) {
                            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, TransactionState.UNKNOWN));
                        } else {
                            log.error("Unhandled exception. Exception details: " + throwable.getMessage());
                        }
                    })
                    .take(1)
                    .subscribe(transaction -> {
                        if (transaction != null) {
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, false, transaction));
                            log.info("usubscribing from receiveTransactions");
                        } else {
                            log.error("received transaction is null!");
                        }
                    });

            // Add subscription to the list of subscriptions
            final Subscription sub = new ObservableSubscription(subscription, SubscriptionType.RECEIVE_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(subscriptionId, sub);
        } catch (BlockchainIdNotFoundException e) {
            // This (should only) happen when the blockchainId is not found
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, TransactionState.UNKNOWN));
        }
    }

    /**
     * Detects that a specific mined transasction got orphaned. Sends a callback when this is detected.
     * Resulting states:
     * <p>
     * UNKNOWN: the blockchain network is not recognized, or connection to node is not possible.
     * PENDING: the transaction became orphaned
     * NOT_FOUND: the given transaction id is not found (wrong id?)
     * <p>
     * (ASSUMES INPUT TRANSACTION TO BE MINED)
     *
     * @param subscriptionId supplied by the remote application as a means for correlation
     * @param transactionId  the hash of the transaction to monitor
     * @param blockchainId   the blockchain network id
     * @param epUrl          the url of the endpoint to send the callback message to
     */
    public void detectOrphanedTransaction(final String subscriptionId, final String transactionId, final String blockchainId,
                                          final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<TransactionState> future = adapter.detectOrphanedTransaction(transactionId);
            future.
                    thenAccept(txState -> {
                        if (txState != null) {
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, false, txState));
                        } else // we should never reach here!
                            log.error("resulting transactionState is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to monitor a transaction. Reason: {}", e.getMessage());
                        // This happens when a communication error, or an error with the tx exist.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, false, TransactionState.UNKNOWN));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(subscriptionId);
                    });
            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.DETECT_ORPHANED_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(subscriptionId, subscription);
        } catch (BlockchainIdNotFoundException e) {
            // This (should only) happen when the blockchainId is not found
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, false, TransactionState.UNKNOWN));
        }
    }

    /**
     * Detects that a specific transasction got enough block-confirmations. It sends also sends a callback if the transaction
     * is not found (probably due to invalidation)
     * Resulting states:
     * <p>
     * UNKNOWN: the blockchain network is not recognized, or connection to node is not possible.
     * CONFIRMED: the transaction received enough block-confirmations
     * NOT_FOUND: the given transaction id is not found (either did not exist, or became invalid after being orphaned)
     * <p>
     * (ASSUMES INPUT TRANSACTION TO BE MINED)
     *
     * @param subscriptionId supplied by the remote application as a means for correlation
     * @param transactionId  the hash of the transaction to monitor
     * @param blockchainId   the blockchain network id
     * @param epUrl          the url of the endpoint to send the callback message to
     */
    public void ensureTransactionState(final String subscriptionId, final String transactionId, final String blockchainId,
                                       final long waitFor, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<TransactionState> future = adapter.ensureTransactionState(waitFor, transactionId);
            future.
                    thenAccept(txState -> {
                        if (txState != null) {
                            if (txState == TransactionState.CONFIRMED)
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, false, txState));
                            else
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, txState));
                        } else // we should never reach here!
                            log.error("resulting transactionState is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to monitor a transaction. Reason: {}", e.getMessage());
                        // This happens when a communication error, or an error with the tx exist.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, TransactionState.UNKNOWN));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(subscriptionId);
                    });
            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.ENSURE_TRANSACTION_STATE);
            SubscriptionManager.getInstance().createSubscription(subscriptionId, subscription);
        } catch (BlockchainIdNotFoundException e) {
            // This (should only) happen when the blockchainId is not found
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(subscriptionId, true, TransactionState.UNKNOWN));
        }
    }
}
