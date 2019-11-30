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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.utils.MathUtils;
import blockchains.iaas.uni.stuttgart.de.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainIdNotFoundException;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainNodeUnreachableException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidScipParameterException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.exceptions.TransactionNotFoundException;
import blockchains.iaas.uni.stuttgart.de.management.callback.CallbackManager;
import blockchains.iaas.uni.stuttgart.de.management.callback.CamundaMessageTranslator;
import blockchains.iaas.uni.stuttgart.de.management.callback.ScipMessageTranslator;
import blockchains.iaas.uni.stuttgart.de.management.model.CompletableFutureSubscription;
import blockchains.iaas.uni.stuttgart.de.management.model.MonitorOccurrencesSubscription;
import blockchains.iaas.uni.stuttgart.de.management.model.ObservableSubscription;
import blockchains.iaas.uni.stuttgart.de.management.model.Subscription;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionKey;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import com.google.common.base.Strings;
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
                                        CamundaMessageTranslator.convert(correlationId, tx, false));
                            } else {// it is NOT_FOUND
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        CamundaMessageTranslator.convert(correlationId, tx, true));
                            }
                        } else
                            log.info("resulting transaction is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to submit a transaction. Reason: {}", e.getMessage());
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, ((BlockchainNodeUnreachableException) e).getCode()));
                        else if (e.getCause() instanceof InvalidTransactionException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, TransactionState.INVALID, true, ((InvalidTransactionException) e).getCode()));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId, blockchainId);
                    });

            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.SUBMIT_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(correlationId, blockchainId, subscription);
        } catch (InvalidTransactionException e) {
            // This (should only) happen when something is wrong with the transaction data
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    CamundaMessageTranslator.convert(correlationId, TransactionState.INVALID, true, e.getCode()));
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, e.getCode()));
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
                        SubscriptionManager.getInstance().removeSubscription(correlationId, blockchainId);
                    })
                    .doOnError(throwable -> log.error("Failed to receive transaction. Reason:{}", throwable.getMessage()))
                    .subscribe(transaction -> {
                        if (transaction != null) {
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, transaction, false));
                        } else {
                            log.error("received transaction is null!");
                        }
                    });

            // Add subscription to the list of subscriptions
            final Subscription sub = new ObservableSubscription(subscription, SubscriptionType.RECEIVE_TRANSACTIONS);
            SubscriptionManager.getInstance().createSubscription(correlationId, blockchainId, sub);
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
                        SubscriptionManager.getInstance().removeSubscription(correlationId, blockchainId);
                    })
                    .doOnError(throwable -> {
                        log.error("Failed to receive transaction. Reason: " + throwable.getMessage());

                        if (throwable instanceof BlockchainNodeUnreachableException || throwable.getCause() instanceof BlockchainNodeUnreachableException) {
                            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, (new BlockchainNodeUnreachableException()).getCode()));
                        } else {
                            log.error("Unhandled exception. Exception details: " + throwable.getMessage());
                        }
                    })
                    .take(1)
                    .subscribe(transaction -> {
                        if (transaction != null) {
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, transaction, false));
                            log.info("usubscribing from receiveTransactions");
                        } else {
                            log.error("received transaction is null!");
                        }
                    });

            // Add subscription to the list of subscriptions
            final Subscription sub = new ObservableSubscription(subscription, SubscriptionType.RECEIVE_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(correlationId, blockchainId, sub);
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found Or
            // if trying to receive a monetary transaction via, e.g., Fabric
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, e.getCode()));
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
                                    CamundaMessageTranslator.convert(correlationId, txState, false, 0));
                        } else // we should never reach here!
                            log.error("resulting transactionState is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to monitor a transaction. Reason: {}", e.getMessage());
                        // This happens when a communication error, or an error with the tx exist.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, false, 0));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId, blockchainId);
                    });
            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.DETECT_ORPHANED_TRANSACTION);
            SubscriptionManager.getInstance().createSubscription(correlationId, blockchainId, subscription);
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found Or
            // if trying to receive a monetary transaction via, e.g., Fabric
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, false, 0));
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
                                        CamundaMessageTranslator.convert(correlationId, txState, false, 0));
                            else
                                CallbackManager.getInstance().sendCallback(epUrl,
                                        CamundaMessageTranslator.convert(correlationId, txState, true, 0));
                        } else // we should never reach here!
                            log.error("resulting transactionState is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to monitor a transaction. Reason: {}", e.getMessage());
                        // This happens when a communication error, or an error with the tx exist.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            CallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, ((BlockchainNodeUnreachableException) e.getCause()).getCode()));

                        // ManualUnsubscriptionException is also captured here
                        return null;
                    }).
                    whenComplete((r, e) -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId, blockchainId);
                    });
            // Add subscription to the list of subscriptions
            final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.ENSURE_TRANSACTION_STATE);
            SubscriptionManager.getInstance().createSubscription(correlationId, blockchainId, subscription);
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found Or
            // if trying to monitor a monetary transaction via, e.g., Fabric
            CallbackManager.getInstance().sendCallbackAsync(epUrl,
                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, e.getCode()));
        }
    }

    // todo add support for timeouts
    // todo add support for signature checking

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
            final String signature) throws BalException {

        // Validate scip parameters!
        if (Strings.isNullOrEmpty(blockchainIdentifier)
                || Strings.isNullOrEmpty(smartContractPath)
                || Strings.isNullOrEmpty(functionIdentifier)
                || timeoutMillis < 0
                || MathUtils.doubleCompare(requiredConfidence, 0.0) < 0
                || MathUtils.doubleCompare(requiredConfidence, 100.0) > 0) {
            throw new InvalidScipParameterException();
        }

        final AdapterManager adapterManager = AdapterManager.getInstance();
        final double minimumConfidenceAsProbability = requiredConfidence / 100.0;

        final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainIdentifier);
        final CompletableFuture<Transaction> future = adapter.invokeSmartContract(smartContractPath,
                functionIdentifier, inputs, outputs, minimumConfidenceAsProbability);

        future.
                thenAccept(tx -> {
                    if (tx != null) {
                        if (tx.getState() == TransactionState.CONFIRMED || tx.getState() == TransactionState.RETURN_VALUE) {
                            CallbackManager.getInstance().sendCallback(callbackUrl,
                                    ScipMessageTranslator.getInvocationResponseMessage(
                                            correlationId,
                                            tx.getReturnValues()));
                        } else {// it is NOT_FOUND (it was dropped from the system due to invalidation)
                            CallbackManager.getInstance().sendCallback(callbackUrl,
                                    ScipMessageTranslator.getAsynchronousErrorResponseMessage(
                                            correlationId,
                                            new TransactionNotFoundException("The transaction associated with an function invocation is invalidated after it was mined.")));
                        }
                    } else
                        log.info("resulting transaction is null");
                }).
                exceptionally((e) -> {
                    log.info("Failed to invoke smart contract function. Reason: {}", e.getMessage());
                    // happens if the node is unreachable, or something goes wrong while trying to invoke the sc function.
                    if (e.getCause() instanceof BalException)
                        CallbackManager.getInstance().sendCallback(callbackUrl,
                                ScipMessageTranslator.getAsynchronousErrorResponseMessage(correlationId, (BalException) e.getCause()));

                    // ManualUnsubscriptionException is also captured here
                    return null;
                }).
                whenComplete((r, e) -> {
                    // remove subscription from subscription list
                    SubscriptionManager.getInstance().removeSubscription(correlationId, blockchainIdentifier, smartContractPath);
                });

        // Add subscription to the list of subscriptions
        final Subscription subscription = new CompletableFutureSubscription<>(future, SubscriptionType.INVOKE_SMART_CONTRACT_FUNCTION);
        SubscriptionManager.getInstance().createSubscription(correlationId, blockchainIdentifier, smartContractPath, subscription);
    }

    public void subscribeToEvent(
            final String blockchainIdentifier,
            final String smartContractPath,
            final String eventIdentifier,
            final List<Parameter> outputParameters,
            final double degreeOfConfidence,
            final String filter,
            final String callbackUrl,
            final String correlationIdentifier) {

        // Validate scip parameters!
        if (Strings.isNullOrEmpty(blockchainIdentifier)
                || Strings.isNullOrEmpty(smartContractPath)
                || Strings.isNullOrEmpty(eventIdentifier)
                || MathUtils.doubleCompare(degreeOfConfidence, 0.0) < 0
                || MathUtils.doubleCompare(degreeOfConfidence, 100.0) > 0) {
            throw new InvalidScipParameterException();
        }

        final double minimumConfidenceAsProbability = degreeOfConfidence / 100.0;

        // first, we cancel previous identical subscriptions.
        this.cancelEventSubscriptions(blockchainIdentifier, smartContractPath, correlationIdentifier, eventIdentifier, outputParameters);
        Disposable result = AdapterManager.getInstance().getAdapter(blockchainIdentifier)
                .subscribeToEvent(smartContractPath, eventIdentifier, outputParameters, minimumConfidenceAsProbability, filter)
                .doFinally(() -> {
                    // remove subscription from subscription list
                    SubscriptionManager.getInstance().removeSubscription(correlationIdentifier, blockchainIdentifier, smartContractPath);
                })
                .doOnError(throwable -> log.error("Failed to detect an occurrence. Reason:{}", throwable.getMessage()))
                .subscribe(occurrence -> {
                    if (occurrence != null) {
                        CallbackManager.getInstance().sendCallback(callbackUrl,
                                ScipMessageTranslator.getSubscriptionResponseMessage(correlationIdentifier, occurrence.getParameters(), occurrence.getIsoTimestamp()));
                    } else {
                        log.error("detected occurrence is null!");
                    }
                });

        // Add subscription to the list of subscriptions
        final Subscription subscription = new MonitorOccurrencesSubscription(result, SubscriptionType.EVENT_OCCURRENCES, eventIdentifier, outputParameters);
        SubscriptionManager.getInstance().createSubscription(correlationIdentifier, blockchainIdentifier, smartContractPath, subscription);
    }

    public void cancelEventSubscriptions(String blockchainId, String smartContractId, String correlationId, String eventIdentifier, List<Parameter> parameters) {
        // Validate scip parameters!
        if (Strings.isNullOrEmpty(blockchainId) || Strings.isNullOrEmpty(smartContractId)) {
            throw new InvalidScipParameterException();
        }

        // get all subscription keys with the specified eventId and params of the current blockchain and smart contract.
        Collection<SubscriptionKey> keys = SubscriptionManager.getInstance().getAllSubscriptionIdsOfEvent(blockchainId, smartContractId, eventIdentifier, parameters);
        this.cancelSubscriptions(correlationId, keys);
    }

    public void cancelFunctionSubscriptions(String blockchainId, String smartContractId, String correlationId, String functionIdentifier, List<Parameter> parameters) {
        // Validate scip parameters!
        if (Strings.isNullOrEmpty(blockchainId) || Strings.isNullOrEmpty(smartContractId)) {
            throw new InvalidScipParameterException();
        }

        // get all subscription keys with the specified eventId and params of the current blockchain and smart contract.
        Collection<SubscriptionKey> keys = SubscriptionManager.getInstance().getAllSubscriptionIdsOfFunction(blockchainId, smartContractId, functionIdentifier, parameters);
        this.cancelSubscriptions(correlationId, keys);
    }

    /**
     * Tests whether the connection with the specified blockchain instance is functioning correctly
     *
     * @param blockchainIdentifier the identifier of the blockchain instance to test.
     * @return true if the connection is functional, false otherwise.
     */
    public String testConnection(String blockchainIdentifier) {
        try {
            final BlockchainAdapter adapter = AdapterManager.getInstance().getAdapter(blockchainIdentifier);
            return adapter.testConnection();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private void cancelSubscriptions(String correlationId, Collection<SubscriptionKey> keys) {
        // here, we just unsubscribe. The Blockchain Manager removes subscriptions from the list.
        for (SubscriptionKey key : keys) {
            // if the correlation id is provided, only remove subscriptions that has it.
            if (!Strings.isNullOrEmpty(correlationId)) {
                if (key.getCorrelationId().equals(correlationId)) {
                    SubscriptionManager.getInstance().getSubscription(key.getCorrelationId(), key.getBlockchainId(), key.getSmartContractPath()).unsubscribe();
                }
            } else {
                SubscriptionManager.getInstance().getSubscription(key.getCorrelationId(), key.getBlockchainId(), key.getSmartContractPath()).unsubscribe();
            }
        }
    }
}
