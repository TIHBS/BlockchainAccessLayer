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
package blockchains.iaas.uni.stuttgart.de.management;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainIdNotFoundException;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainNodeUnreachableException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvokeSmartContractFunctionFailure;
import blockchains.iaas.uni.stuttgart.de.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.management.callback.CallbackManager;
import blockchains.iaas.uni.stuttgart.de.management.callback.MessageTranslatorFactory;
import blockchains.iaas.uni.stuttgart.de.management.model.CompletableFutureSubscription;
import blockchains.iaas.uni.stuttgart.de.management.model.ObservableSubscription;
import blockchains.iaas.uni.stuttgart.de.management.model.Subscription;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param correlationId      supplied by the remote application as a means for correlation
     * @param to                 the address of the transaction recipient
     * @param value              the value of the transaction
     * @param blockchainId       the blockchain network id
     * @param requiredConfidence the degree-of-confidence required to be achieved before sending a callback message to the invoker.
     * @param epUrl              the url of the endpoint to send the callback message to
     */
    public void submitNewTransaction(final String correlationId, final String to, final BigInteger value,
                                     final String blockchainId, final double requiredConfidence, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();

        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<Transaction> future = adapter.submitTransaction(to, new BigDecimal(value), requiredConfidence);
            // This happens when a communication error, or an error with the tx exist.
            future.
                    thenAccept(tx -> {
                        if (tx != null) {
                            if (tx.getState() == TransactionState.CONFIRMED) {
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, tx, false));
                            } else {// it is NOT_FOUND
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, tx, true));
                            }
                        } else
                            log.info("resulting transaction is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to submit a transaction. Reason: {}", e.getMessage());
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, true, ((BlockchainNodeUnreachableException) e).getCode()));
                        else if (e.getCause() instanceof InvalidTransactionException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.INVALID, true, ((InvalidTransactionException) e).getCode()));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId);
                    });

            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.SUBMIT_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(correlationId, subscription);
        } catch (InvalidTransactionException e) {
            // This (should only) happen when something is wrong with the transaction data
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.INVALID, true, e.getCode()));
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, true, e.getCode()));
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
     * @param correlationId      supplied by the remote application as a means for correlation
     * @param from               an optional parameter.If supplied, it indicates the sending address of the transactions we are interested
     *                           in.
     * @param blockchainId       the blockchain network id
     * @param requiredConfidence the degree-of-confidence required to be achieved before sending a callback message to the invoker.
     * @param epUrl              the url of the endpoint to send the callback message to
     */
    public void receiveTransactions(final String correlationId, final String from, final String blockchainId,
                                    final double requiredConfidence, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final Disposable subscription = adapter.receiveTransactions(from, requiredConfidence)
                    .doFinally(() -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId);
                    })
                    .doOnError(throwable -> log.error("Failed to receive transaction. Reason:{}", throwable.getMessage()))
                    .subscribe(transaction -> {
                        if (transaction != null) {
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, transaction, false));
                        } else {
                            log.error("received transaction is null!");
                        }
                    });

            // Add subscription to the list of subscriptions
            final Subscription sub = new ObservableSubscription(subscription, SubscriptionType.RECEIVE_TRANSACTIONS);
            SubscriptionManager.getInstance().createSubscription(correlationId, sub);
        } catch (BlockchainIdNotFoundException e) {
            // This (should only) happen when the blockchainId is not found
            log.error("blockchainId ({}) is not recognized, but no error callback is sent to endpoint!", blockchainId);
        } catch (NotSupportedException e) {
            // trying to receive monetary transactions on, e.g., Fabric.
            log.error(e.getMessage());
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
     * @param correlationId      supplied by the remote application as a means for correlation
     * @param from               an optional parameter.If supplied, it indicates the sending address of the transactions we are interested
     *                           in.
     * @param blockchainId       the blockchain network id
     * @param requiredConfidence the degree-of-confidence required to be achieved before sending a callback message to the invoker.
     * @param epUrl              the url of the endpoint to send the callback message to
     */
    public void receiveTransaction(final String correlationId, final String from, final String blockchainId,
                                   final double requiredConfidence, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final Disposable subscription = adapter.receiveTransactions(from, requiredConfidence)
                    .doFinally(() -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId);
                    })
                    .doOnError(throwable -> {
                        log.error("Failed to receive transaction. Reason: " + throwable.getMessage());

                        if (throwable instanceof BlockchainNodeUnreachableException || throwable.getCause() instanceof BlockchainNodeUnreachableException) {
                            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, true, (new BlockchainNodeUnreachableException()).getCode()));
                        } else {
                            log.error("Unhandled exception. Exception details: " + throwable.getMessage());
                        }
                    })
                    .take(1)
                    .subscribe(transaction -> {
                        if (transaction != null) {
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, transaction, false));
                            log.info("usubscribing from receiveTransactions");
                        } else {
                            log.error("received transaction is null!");
                        }
                    });

            // Add subscription to the list of subscriptions
            final Subscription sub = new ObservableSubscription(subscription, SubscriptionType.RECEIVE_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(correlationId, sub);
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found Or
            // if trying to receive a monetary transaction via, e.g., Fabric
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, true, e.getCode()));
        }
    }

    /**
     * Detects that a specific mined transaction got orphaned. Sends a callback when this is detected.
     * Resulting states:
     * <p>
     * UNKNOWN: the blockchain network is not recognized, or connection to node is not possible.
     * PENDING: the transaction became orphaned
     * NOT_FOUND: the given transaction id is not found (wrong id?)
     * <p>
     * (ASSUMES INPUT TRANSACTION TO BE MINED)
     *
     * @param correlationId supplied by the remote application as a means for correlation
     * @param transactionId the hash of the transaction to monitor
     * @param blockchainId  the blockchain network id
     * @param epUrl         the url of the endpoint to send the callback message to
     */
    public void detectOrphanedTransaction(final String correlationId, final String transactionId, final String blockchainId,
                                          final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<TransactionState> future = adapter.detectOrphanedTransaction(transactionId);
            future.
                    thenAccept(txState -> {
                        if (txState != null) {
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, txState, false, 0));
                        } else // we should never reach here!
                            log.error("resulting transactionState is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to monitor a transaction. Reason: {}", e.getMessage());
                        // This happens when a communication error, or an error with the tx exist.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, false, 0));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId);
                    });
            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.DETECT_ORPHANED_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(correlationId, subscription);
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found Or
            // if trying to receive a monetary transaction via, e.g., Fabric
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, false, 0));
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
     * @param correlationId      supplied by the remote application as a means for correlation
     * @param transactionId      the hash of the transaction to monitor
     * @param blockchainId       the blockchain network id
     * @param requiredConfidence the degree-of-confidence required to be achieved before sending a callback message to the invoker.
     * @param epUrl              the url of the endpoint to send the callback message to
     */
    public void ensureTransactionState(final String correlationId, final String transactionId, final String blockchainId,
                                       final double requiredConfidence, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<TransactionState> future = adapter.ensureTransactionState(transactionId, requiredConfidence);
            future.
                    thenAccept(txState -> {
                        if (txState != null) {
                            if (txState == TransactionState.CONFIRMED)
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, txState, false, 0));
                            else
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, txState, true, 0));
                        } else // we should never reach here!
                            log.error("resulting transactionState is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to monitor a transaction. Reason: {}", e.getMessage());
                        // This happens when a communication error, or an error with the tx exist.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, true, ((BlockchainNodeUnreachableException) e.getCause()).getCode()));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId);
                    });
            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.ENSURE_TRANSACTION_STATE);
            SubscriptionManager.getInstance().createSubscription(correlationId, subscription);
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found Or
            // if trying to monitor a monetary transaction via, e.g., Fabric
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, true, e.getCode()));
        }
    }

    /**
     * Invokes a smart contract function, and sends a callback message informing a remote endpoint of the result.
     * The invocation might require the submission of a transaction if the invoked function is not read-only.
     * The status of the result could be:
     * <p>
     * UNKNOWN: the blockchain network is not recognized, or connection to node is not possible or the function is not recognized
     * INVALID: the submitted transaction failed validation at the node (if a transaction was required)
     * CONFIRMED (along with the tx itself): the submitted transaction received the desired number of block-confirmations
     * or the result returned from a read-only smart contract function.
     *
     * @param blockchainIdentifier the unique identifier of the blockchain system we
     * @param smartContractPath    the technology-specific path to the smart contract
     * @param functionIdentifier   the function name
     * @param inputs               the arguments to be passed to the smart contract function
     * @param outputs              the types of output parameters expected from the function
     * @param requiredConfidence   the minimum confidence that the submitted transaction must reach before returning a CONFIRMED response (percentage)
     * @param callbackUrl          the url of the endpoint to send the callback message to
     * @param timeoutMillis        the number of milliseconds during which the doc must be reached, otherwise a timeout error message has to be returned.
     * @param correlationId        applied by the remote application as a means for correlation
     * @param signature            the user signature of the previous fields (apart from smart contract path)
     */
    public void invokeSmartContractFunction(
            final String blockchainIdentifier,
            final String smartContractPath,
            final String functionIdentifier,
            final List<Parameter> inputs,
            final List<Parameter> outputs,
            final double requiredConfidence,
            final String callbackUrl,
            final long timeoutMillis,
            final String correlationId,
            final String signature) {
        final AdapterManager adapterManager = AdapterManager.getInstance();
        final double minimumConfidenceAsProbability = requiredConfidence / 100.0;

        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainIdentifier);
            final CompletableFuture<Transaction> future = adapter.invokeSmartContract(smartContractPath,
                    functionIdentifier, inputs, outputs, requiredConfidence);

            future.
                    thenAccept(tx -> {
                        if (tx != null) {
                            if (tx.getState() == TransactionState.CONFIRMED || tx.getState() == TransactionState.RETURN_VALUE) {
                                CallbackManager.getInstance().sendCallback(callbackUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, tx, false));
                            } else {// it is NOT_FOUND todo find out what kind of an error is caught here
                                CallbackManager.getInstance().sendCallback(callbackUrl,
                                        MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, tx, true));
                            }
                        } else
                            log.info("resulting transaction is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to invoke smart contract function. Reason: {}", e.getMessage());
                        // happens if the node is unreachable, or something goes wrong while trying to invoke the sc function.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException ||
                                e.getCause() instanceof InvokeSmartContractFunctionFailure)
                            CallbackManager.getInstance().sendCallback(callbackUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, true, ((BalException) e.getCause()).getCode()));
                        else if (e.getCause() instanceof InvalidTransactionException)
                            CallbackManager.getInstance().sendCallback(callbackUrl,
                                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.INVALID, true, ((InvalidTransactionException) e.getCause()).getCode()));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId);
                    });

            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.INVOKE_SMART_CONTRACT_FUNCTION);
            SubscriptionManager.getInstance().createSubscription(correlationId, subscription);
        } catch (InvalidTransactionException | NotSupportedException e) {
            // This (should only) happen when something is wrong with the transaction data OR
            // when the underlying blockchain does not support smart contract function invocations (like Bitcoin)
            CallbackManager.getInstance().sendCallbackAsync(callbackUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.INVALID, true, e.getCode()));
        } catch (BlockchainIdNotFoundException e) {
            // This (should only) happen when the blockchainId is not found
            CallbackManager.getInstance().sendCallbackAsync(callbackUrl,
                    MessageTranslatorFactory.getCallbackAdapter().convert(correlationId, TransactionState.UNKNOWN, true, e.getCode()));
        }
    }

    /**
     * Tests whether the connection with the specified blockchain instance is functioning correctly
     *
     * @param blockchainIdentifier the identifier of the blockchain instance to test.
     * @return true if the connection is functional, false otherwise.
     */
    public boolean testConnection(String blockchainIdentifier) {
        try {
            final BlockchainAdapter adapter = AdapterManager.getInstance().getAdapter(blockchainIdentifier);
            return adapter.testConnection();
        } catch (Exception e) {
            return false;
        }
    }
}
