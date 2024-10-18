/********************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.*;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.api.model.*;
import blockchains.iaas.uni.stuttgart.de.api.utils.MathUtils;
import blockchains.iaas.uni.stuttgart.de.restapi.callback.CamundaMessageTranslator;
import blockchains.iaas.uni.stuttgart.de.restapi.callback.RestCallbackManager;
import blockchains.iaas.uni.stuttgart.de.scip.callback.ScipCallbackManager;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.Argument;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.InvokeResponse;
import blockchains.iaas.uni.stuttgart.de.scip.model.responses.SubscribeResponse;
import blockchains.iaas.uni.stuttgart.de.subscription.SubscriptionManager;
import blockchains.iaas.uni.stuttgart.de.subscription.model.*;
import com.google.common.base.Strings;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Log4j2
@Component
public class BlockchainManager {
    private final AdapterManager adapterManager;

    public BlockchainManager(AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    private static @NotNull AsynchronousBalException generateAsynchronousBalException(String correlationId, Transaction tx) {
        AsynchronousBalException exception;
        if (tx.getState() == TransactionState.NOT_FOUND) {
            TransactionNotFoundException txNotFoundException =
                    new TransactionNotFoundException("The transaction associated with a function invocation is invalidated after it was mined.");
            exception = new AsynchronousBalException(txNotFoundException, correlationId);
        } else {
            InvokeSmartContractFunctionFailure invocationException =
                    new InvokeSmartContractFunctionFailure("The smart contract function invocation reported an error.");
            exception = new AsynchronousBalException(invocationException, correlationId);
        }
        return exception;
    }

    /**
     * Submits a transaction to the blockchain, and sends a callback message informing a remote endpoint of the result.
     * The status of the result could be:
     * <p>
     * UNKNOWN: the blockchain network is not recognized, or connection to node is not possible.
     * INVALID: the submitted transaction failed validation at the node
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
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<Transaction> future = adapter.submitTransaction(to, new BigDecimal(value), requiredConfidence);
            // This happens when a communication error, or an error with the tx exist.
            future.
                    thenAccept(tx -> {
                        if (tx != null) {
                            if (tx.getState() == TransactionState.CONFIRMED) {
                                RestCallbackManager.getInstance().sendCallback(epUrl,
                                        CamundaMessageTranslator.convert(correlationId, tx, false));
                            } else {// it is NOT_FOUND
                                RestCallbackManager.getInstance().sendCallback(epUrl,
                                        CamundaMessageTranslator.convert(correlationId, tx, true));
                            }
                        } else
                            log.info("resulting transaction is null");
                    }).
                    exceptionally((e) -> {
                        log.error("Failed to submit a transaction.", e);
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            RestCallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, ((BlockchainNodeUnreachableException) e).getCode()));
                        else if (e.getCause() instanceof InvalidTransactionException)
                            RestCallbackManager.getInstance().sendCallback(epUrl,
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
            RestCallbackManager.getInstance().sendCallbackAsync(epUrl,
                    CamundaMessageTranslator.convert(correlationId, TransactionState.INVALID, true, e.getCode()));
        } catch (BlockchainIdNotFoundException | NotSupportedException e) {
            // This (should only) happen when the blockchainId is not found
            RestCallbackManager.getInstance().sendCallbackAsync(epUrl,
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
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final Disposable subscription = adapter.receiveTransactions(from, requiredConfidence)
                    .doFinally(() -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId, blockchainId);
                    })
                    .doOnError(throwable -> log.error("Failed to receive transaction.", throwable))
                    .subscribe(transaction -> {
                        if (transaction != null) {
                            RestCallbackManager.getInstance().sendCallback(epUrl,
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
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final Disposable subscription = adapter.receiveTransactions(from, requiredConfidence)
                    .doFinally(() -> {
                        // remove subscription from subscription list
                        SubscriptionManager.getInstance().removeSubscription(correlationId, blockchainId);
                    })
                    .doOnError(throwable -> {
                        log.error("Failed to receive transaction.", throwable);

                        if (throwable instanceof BlockchainNodeUnreachableException || throwable.getCause() instanceof BlockchainNodeUnreachableException) {
                            RestCallbackManager.getInstance().sendCallbackAsync(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, (new BlockchainNodeUnreachableException()).getCode()));
                        } else {
                            log.error("Unhandled exception.", throwable);
                        }
                    })
                    .take(1)
                    .subscribe(transaction -> {
                        if (transaction != null) {
                            RestCallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, transaction, false));
                            log.info("Usubscribing from receiveTransactions");
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
            RestCallbackManager.getInstance().sendCallbackAsync(epUrl,
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
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<TransactionState> future = adapter.detectOrphanedTransaction(transactionId);
            future.
                    thenAccept(txState -> {
                        if (txState != null) {
                            RestCallbackManager.getInstance().sendCallback(epUrl,
                                    CamundaMessageTranslator.convert(correlationId, txState, false, 0));
                        } else // we should never reach here!
                            log.error("Resulting transactionState is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to monitor a transaction.", e);
                        // This happens when a communication error, or an error with the tx exist.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            RestCallbackManager.getInstance().sendCallback(epUrl,
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
            RestCallbackManager.getInstance().sendCallbackAsync(epUrl,
                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, false, 0));
        }
    }

    // todo add support for timeouts
    // todo add support for signature checking
    // todo add support for nonce

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
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final CompletableFuture<TransactionState> future = adapter.ensureTransactionState(transactionId, requiredConfidence);
            future.
                    thenAccept(txState -> {
                        if (txState != null) {
                            if (txState == TransactionState.CONFIRMED)
                                RestCallbackManager.getInstance().sendCallback(epUrl,
                                        CamundaMessageTranslator.convert(correlationId, txState, false, 0));
                            else
                                RestCallbackManager.getInstance().sendCallback(epUrl,
                                        CamundaMessageTranslator.convert(correlationId, txState, true, 0));
                        } else // we should never reach here!
                            log.error("resulting transactionState is null");
                    }).
                    exceptionally((e) -> {
                        log.info("Failed to monitor a transaction.", e);
                        // This happens when a communication error, or an error with the tx exist.
                        if (e.getCause() instanceof BlockchainNodeUnreachableException)
                            RestCallbackManager.getInstance().sendCallback(epUrl,
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
            RestCallbackManager.getInstance().sendCallbackAsync(epUrl,
                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, e.getCode()));
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
     * @param callbackBinding      the binding to use when sending asynchronous callback messages.
     * @param nonce                a monotonically increasing number for the invocation requests sent by the user
     * @param sideEffects          indicates whether the function being invoked might have side effects in the blockchain.
     * @param callbackUrl          the url of the endpoint to send the callback message to
     * @param timeoutMillis        the number of milliseconds during which the doc must be reached, otherwise a timeout error message has to be returned.
     * @param correlationId        applied by the remote application as a means for correlation
     * @param signature            the user's digital signature of the previous fields (apart from smart contract path)
     */
    public void invokeSmartContractFunction(
            final String blockchainIdentifier,
            final String smartContractPath,
            final String functionIdentifier,
            final List<Parameter> inputs,
            final List<Parameter> outputs,
            final double requiredConfidence,
            final String callbackBinding,
            final boolean sideEffects,
            final Long nonce,
            final String callbackUrl,
            final long timeoutMillis,
            final String correlationId,
            final String signature) throws BalException {

        final CompletableFuture<Transaction> future = this.invokeSmartContractFunction(blockchainIdentifier, smartContractPath,
                functionIdentifier, inputs, outputs, requiredConfidence, timeoutMillis, signature);

        future.
                thenAccept(tx -> {
                    if (tx != null) {
                        if (callbackUrl != null) {
                            if (tx.getState() == TransactionState.CONFIRMED || tx.getState() == TransactionState.RETURN_VALUE) {
                                if (callbackBinding.isEmpty()) {
                                    RestCallbackManager.getInstance().sendCallback(callbackUrl,
                                            CamundaMessageTranslator.convert(correlationId, tx, false));
                                } else {
                                    InvokeResponse response = InvokeResponse
                                            .builder()
                                            .correlationId(correlationId)
                                            .outputArguments(tx.getReturnValues().stream()
                                                    .map(p -> Argument.builder()
                                                            .name(p.getName()).value(p.getValue())
                                                            .build()
                                                    ).toList())
                                            .build();
                                    ScipCallbackManager.getInstance().sendInvocationResponse(callbackUrl, callbackBinding, response);
                                }
                            } else {// it is NOT_FOUND (it was dropped from the system due to invalidation) or ERRORED
                                AsynchronousBalException exception = generateAsynchronousBalException(correlationId, tx);

                                if (callbackBinding.isEmpty()) {
                                    RestCallbackManager.getInstance().sendCallback(callbackUrl,
                                            CamundaMessageTranslator.convert(correlationId, tx.getState(), true, exception.getCode()));
                                } else {
                                    ScipCallbackManager.getInstance().sendAsyncErrorResponse(callbackUrl, callbackBinding, exception);
                                }
                            }
                        } else {
                            log.info("callbackUrl is null");
                        }
                    } else {
                        log.info("Resulting transaction is null");
                    }
                }).
                exceptionally((e) -> {
                    log.info("Failed to invoke smart contract function.", e);
                    // happens if the node is unreachable, or something goes wrong while trying to invoke the sc function.
                    if (e.getCause() instanceof BalException) {
                        AsynchronousBalException exception =
                                new AsynchronousBalException((BalException) e.getCause(), correlationId);
                        if (callbackBinding.isEmpty()) {
                            RestCallbackManager.getInstance().sendCallback(callbackUrl,
                                    CamundaMessageTranslator.convert(correlationId, TransactionState.UNKNOWN, true, exception.getCode()));
                        } else {
                            ScipCallbackManager.getInstance().sendAsyncErrorResponse(callbackUrl, callbackBinding, exception);
                        }
                    }

                    if (e instanceof ManualUnsubscriptionException || e.getCause() instanceof ManualUnsubscriptionException) {
                        log.info("Manual unsubscription of SC invocation!");
                    }
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

    public CompletableFuture<Transaction> invokeSmartContractFunction(
            final String blockchainIdentifier,
            final String smartContractPath,
            final String functionIdentifier,
            final List<Parameter> inputs,
            final List<Parameter> outputs,
            final double requiredConfidence,
            final long timeoutMillis,
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

        final double minimumConfidenceAsProbability = requiredConfidence / 100.0;
        final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainIdentifier);
        return adapter.invokeSmartContract(smartContractPath,
                functionIdentifier, inputs, outputs, minimumConfidenceAsProbability, timeoutMillis);
    }

    public void subscribeToEvent(
            final String blockchainIdentifier,
            final String smartContractPath,
            final String eventIdentifier,
            final List<Parameter> outputParameters,
            final double degreeOfConfidence,
            final String filter,
            final String callbackBinding,
            final String callbackUrl,
            final String correlationIdentifier) {

        // first, we cancel previous identical subscriptions.
        this.cancelEventSubscriptions(blockchainIdentifier, smartContractPath, correlationIdentifier, eventIdentifier, outputParameters);

        Disposable result = this.subscribeToEvent(blockchainIdentifier, smartContractPath, eventIdentifier, outputParameters, degreeOfConfidence, filter)
                .doFinally(() -> {
                    // remove subscription from subscription list
                    SubscriptionManager.getInstance().removeSubscription(correlationIdentifier, blockchainIdentifier, smartContractPath);
                })
                .doOnError(throwable -> log.error("Failed to detect an occurrence.", throwable))
                .subscribe(occurrence -> {
                    if (occurrence != null) {
                        if (callbackBinding.isEmpty()) {
                            LinearChainTransaction dummy = new LinearChainTransaction();
                            dummy.setReturnValues(occurrence.getParameters());
                            dummy.setState(TransactionState.RETURN_VALUE);
                            RestCallbackManager.getInstance().sendCallback(callbackUrl,
                                    CamundaMessageTranslator.convert(correlationIdentifier, dummy, false));
                        } else {
                            SubscribeResponse response = SubscribeResponse
                                    .builder()
                                    .correlationId(correlationIdentifier)
                                    .timestamp(occurrence.getIsoTimestamp())
                                    .arguments(occurrence
                                            .getParameters()
                                            .stream()
                                            .map(p -> Argument.builder()
                                                    .name(p.getName())
                                                    .value(p.getValue())
                                                    .build()
                                            ).toList())
                                    .build();
                            ScipCallbackManager.getInstance().sendSubscriptionResponse(callbackUrl, callbackBinding, response);
                        }
                    } else {
                        log.error("detected occurrence is null!");
                    }
                });

        // Add subscription to the list of subscriptions
        final Subscription subscription = new MonitorOccurrencesSubscription(result, SubscriptionType.EVENT_OCCURRENCES, eventIdentifier, outputParameters);
        SubscriptionManager.getInstance().createSubscription(correlationIdentifier, blockchainIdentifier, smartContractPath, subscription);
    }

    public Observable<Occurrence> subscribeToEvent(String blockchainIdentifier,
                                                   final String smartContractPath,
                                                   final String eventIdentifier,
                                                   final List<Parameter> outputParameters,
                                                   final double degreeOfConfidence,
                                                   final String filter) {
        // Validate scip parameters!
        if (Strings.isNullOrEmpty(blockchainIdentifier)
                || Strings.isNullOrEmpty(smartContractPath)
                || Strings.isNullOrEmpty(eventIdentifier)
                || MathUtils.doubleCompare(degreeOfConfidence, 0.0) < 0
                || MathUtils.doubleCompare(degreeOfConfidence, 100.0) > 0) {
            throw new InvalidScipParameterException();
        }

        final double minimumConfidenceAsProbability = degreeOfConfidence / 100.0;

        return this.adapterManager.getAdapter(blockchainIdentifier)
                .subscribeToEvent(smartContractPath, eventIdentifier, outputParameters, minimumConfidenceAsProbability, filter);
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

    public QueryResult queryEvents(final String blockchainIdentifier,
                                   final String smartContractPath,
                                   final String eventIdentifier,
                                   final List<Parameter> outputParameters,
                                   final String filter,
                                   final TimeFrame timeFrame) {
        // Validate scip parameters!
        if (Strings.isNullOrEmpty(blockchainIdentifier)
                || Strings.isNullOrEmpty(smartContractPath)
                || Strings.isNullOrEmpty(eventIdentifier)) {
            throw new InvalidScipParameterException();
        }

        try {
            return adapterManager
                    .getAdapter(blockchainIdentifier)
                    .queryEvents(smartContractPath, eventIdentifier, outputParameters, filter, timeFrame)
                    .join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof BalException)
                throw (BalException) e.getCause();

            log.error("caught a non-BALException!", e);
            throw new UnknownException();
        }
    }

    /**
     * Tests whether the connection with the specified blockchain instance is functioning correctly
     *
     * @param blockchainIdentifier the identifier of the blockchain instance to test.
     * @return "true" if the connection is functional, "false" otherwise.
     */
    public String testConnection(String blockchainIdentifier) {
        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainIdentifier);
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
