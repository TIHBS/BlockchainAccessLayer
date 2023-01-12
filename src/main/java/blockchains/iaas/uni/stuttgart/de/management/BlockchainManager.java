/********************************************************************************
 * Copyright (c) 2019-2023 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-author: Akshay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.management;

import blockchains.iaas.uni.stuttgart.de.Utils;
import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.*;
import blockchains.iaas.uni.stuttgart.de.api.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.api.model.*;
import blockchains.iaas.uni.stuttgart.de.api.utils.MathUtils;
import blockchains.iaas.uni.stuttgart.de.management.callback.CallbackManager;
import blockchains.iaas.uni.stuttgart.de.management.callback.CamundaMessageTranslator;
import blockchains.iaas.uni.stuttgart.de.management.callback.ScipMessageTranslator;
import blockchains.iaas.uni.stuttgart.de.management.model.*;
import blockchains.iaas.uni.stuttgart.de.models.PendingTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static blockchains.iaas.uni.stuttgart.de.api.exceptions.ExceptionCode.NotAuthorized;
import static java.util.stream.Collectors.toList;

public class BlockchainManager {
    private static final Logger log = LoggerFactory.getLogger(BlockchainManager.class);
    private Map<String, PendingTransaction> pendingTransactionsMap = new HashMap();
    private static BlockchainManager INSTANCE;

    public static BlockchainManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BlockchainManager();
        }

        return INSTANCE;
    }

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
            final List<String> typeArguments,
            final List<Parameter> inputs,
            final List<Parameter> outputs,
            final double requiredConfidence,
            final String callbackUrl,
            final long timeoutMillis,
            final String correlationId,
            final String signature, final String signer, final List<String> signers, final long minimumNumberOfSignatures) throws BalException {

        // Validate scip parameters!
        if (Strings.isNullOrEmpty(blockchainIdentifier)
                || Strings.isNullOrEmpty(smartContractPath)
                || Strings.isNullOrEmpty(functionIdentifier)
                || timeoutMillis < 0
                || MathUtils.doubleCompare(requiredConfidence, 0.0) < 0
                || MathUtils.doubleCompare(requiredConfidence, 100.0) > 0
                || minimumNumberOfSignatures > signers.size()) {
            throw new InvalidScipParameterException();
        }

        boolean isSignatureValid = Utils.ValidateSignature(correlationId, signer, signature);
        if (!isSignatureValid) {
            throw new InvalidScipParameterException();
        }

        final AdapterManager adapterManager = AdapterManager.getInstance();
        final double minimumConfidenceAsProbability = requiredConfidence / 100.0;
        final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainIdentifier);
        final CompletableFuture<Transaction> future = adapter.invokeSmartContract(smartContractPath,
                functionIdentifier, typeArguments, inputs, outputs, minimumConfidenceAsProbability, timeoutMillis, signature, signer, signers, minimumNumberOfSignatures);

        future.
                thenAccept(tx -> {
                    if (tx != null) {
                        if (tx.getState() == TransactionState.CONFIRMED || tx.getState() == TransactionState.RETURN_VALUE) {
                            CallbackManager.getInstance().sendCallback(callbackUrl,
                                    ScipMessageTranslator.getInvocationResponseMessage(
                                            correlationId,
                                            tx.getReturnValues()));
                        } else {// it is NOT_FOUND (it was dropped from the system due to invalidation) or ERRORED
                            if (tx.getState() == TransactionState.NOT_FOUND) {
                                CallbackManager.getInstance().sendCallback(callbackUrl,
                                        ScipMessageTranslator.getAsynchronousErrorResponseMessage(
                                                correlationId,
                                                new TransactionNotFoundException("The transaction associated with a function invocation is invalidated after it was mined.")));
                            } else {
                                CallbackManager.getInstance().sendCallback(callbackUrl,
                                        ScipMessageTranslator.getAsynchronousErrorResponseMessage(
                                                correlationId,
                                                new InvokeSmartContractFunctionFailure("The smart contract function invocation reported an error.")));
                            }
                        }
                    } else {
                        log.info("resulting transaction is null");
                    }
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
                    pendingTransactionsMap.remove(correlationId);
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
        BlockchainAdapter adapter = AdapterManager.getInstance().getAdapter(blockchainIdentifier);
        if (adapter.canHandleDelegatedSubscription()) {
            // Added in v2.0. If adapter itself can manage the subscriptions, hand over the call to adapter.
            adapter.delegatedSubscribe(smartContractPath, eventIdentifier, outputParameters,
                    degreeOfConfidence, filter, callbackUrl, correlationIdentifier);
        } else {
            // Fallback to initial implementation where subscriptions are managed by the gateway.
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
    }

    public void cancelEventSubscriptions(String blockchainId, String smartContractId, String correlationId, String eventIdentifier, List<Parameter> parameters) {
        // Validate scip parameters!
        if (Strings.isNullOrEmpty(blockchainId) || Strings.isNullOrEmpty(smartContractId)) {
            throw new InvalidScipParameterException();
        }

        BlockchainAdapter adapter = AdapterManager.getInstance().getAdapter(blockchainId);
        if (adapter.canHandleDelegatedSubscription()) {
            // Added in v2.0. If adapter itself can manage the subscriptions, hand over the call to adapter.
            adapter.delegatedUnsubscribe(smartContractId, null, eventIdentifier, null, parameters, correlationId);
        } else {

            // get all subscription keys with the specified eventId and params of the current blockchain and smart contract.
            Collection<SubscriptionKey> keys = SubscriptionManager.getInstance().getAllSubscriptionIdsOfEvent(blockchainId, smartContractId, eventIdentifier, parameters);
            this.cancelSubscriptions(correlationId, keys);
        }
    }

    public void cancelFunctionSubscriptions(String blockchainId, String smartContractId, String correlationId, String functionIdentifier, List<Parameter> parameters, List<String> typeArguments) {
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
                                   final List<String> typeArguments,
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
            return AdapterManager.getInstance()
                    .getAdapter(blockchainIdentifier)
                    .queryEvents(smartContractPath, eventIdentifier, typeArguments, outputParameters, filter, timeFrame)
                    .join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof BalException)
                throw (BalException) e.getCause();

            log.error("caught a non-BALException! " + e.getCause().getClass().getName());
            throw new UnknownException();
        }
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

    public List<PendingTransaction> getPendingInvocations() {
        return pendingTransactionsMap.values().stream().collect(toList());
    }

    public boolean signInvocation(String correlationId, String signature, String signer) {

        if (!pendingTransactionsMap.containsKey(correlationId)) {
            throw new InvocationNotFoundException();
        }

        PendingTransaction pendingTransaction = pendingTransactionsMap.get(correlationId);
        List<String> signers = pendingTransaction.getSigners();
        if (!signers.contains(signer)) {
            return false;
        }

        String invocationHash = pendingTransactionsMap.get(correlationId).getInvocationHash();
        boolean isSignatureValid = Utils.ValidateSignature(invocationHash, signer, signature);
        if (!isSignatureValid) {
            return false;
        }

        pendingTransaction.getSignatures().add(signature);


        if (pendingTransaction.getSignatures().size() >= pendingTransaction.getMinimumNumberOfSignatures()) {
            invokeSmartContractFunction(pendingTransaction.getBlockchainIdentifier(),
                    pendingTransaction.getSmartContractPath(),
                    pendingTransaction.getFunctionIdentifier(),
                    pendingTransaction.getTypeArguments(),
                    pendingTransaction.getInputs(),
                    pendingTransaction.getOutputs(),
                    pendingTransaction.getRequiredConfidence(),
                    pendingTransaction.getCallbackUrl(),
                    pendingTransaction.getTimeoutMillis(),
                    pendingTransaction.getCorrelationIdentifier(),
                    pendingTransaction.getSignature(),
                    pendingTransaction.getProposer(),
                    pendingTransaction.getSigners(),
                    pendingTransaction.getMinimumNumberOfSignatures());
        }
        return true;
    }

    public boolean tryCancelInvocation(String correlationId, String signature, String signer) {

        if (!pendingTransactionsMap.containsKey(correlationId)) {
            throw new InvocationNotFoundException();
        }

        String invocationHash = pendingTransactionsMap.get(correlationId).getInvocationHash();
        boolean isSignatureValid = Utils.ValidateSignature(invocationHash, signer, signature);
        if (!isSignatureValid) {
            return false;
        }
        if (pendingTransactionsMap.containsKey(correlationId)) {
            pendingTransactionsMap.remove(correlationId);
            return true;
        } else {
            return false;
        }
    }

    public boolean tryReplaceInvocation(final String blockchainIdentifier,
                                        final String smartContractPath,
                                        final String functionIdentifier,
                                        final List<String> typeArguments,
                                        final List<Parameter> inputs,
                                        final List<Parameter> outputs,
                                        final double requiredConfidence,
                                        final String callbackUrl,
                                        final long timeoutMillis,
                                        final String correlationId,
                                        final String signature, final String proposer, final List<String> signers, final long minimumNumberOfSignatures) {


        if (!pendingTransactionsMap.containsKey(correlationId)) {
            throw new InvocationNotFoundException();
        }

        String invocationHash = pendingTransactionsMap.get(correlationId).getInvocationHash();
        boolean isSignatureValid = Utils.ValidateSignature(invocationHash, proposer, signature);
        if (!isSignatureValid) {
            return false;
        }

        PendingTransaction p = new PendingTransaction();
        p.setBlockchainIdentifier(blockchainIdentifier);
        p.setSmartContractPath(smartContractPath);
        p.setFunctionIdentifier(functionIdentifier);
        p.setInputs(inputs);
        p.setOutputs(outputs);
        p.setSigners(signers);
        p.setCorrelationIdentifier(correlationId);
        p.setMinimumNumberOfSignatures(minimumNumberOfSignatures);
        p.setTypeArguments(typeArguments);
        p.setRequiredConfidence(requiredConfidence);
        p.setCallbackUrl(callbackUrl);
        p.setSignature(signature);
        p.setTimeoutMillis(timeoutMillis);
        p.setSignatures(new ArrayList<>());
        p.setProposer(proposer);

        String newInvocationHash = generateInvocationHash(blockchainIdentifier, smartContractPath, functionIdentifier,
                typeArguments, inputs, outputs, requiredConfidence, callbackUrl, timeoutMillis, correlationId,
                signature, proposer, signers, minimumNumberOfSignatures);
        p.setInvocationHash(newInvocationHash);

        pendingTransactionsMap.replace(correlationId, p);

        return true;


    }

    public void createPendingInvocation(final String blockchainIdentifier,
                                        final String smartContractPath,
                                        final String functionIdentifier,
                                        final List<String> typeArguments,
                                        final List<Parameter> inputs,
                                        final List<Parameter> outputs,
                                        final double requiredConfidence,
                                        final String callbackUrl,
                                        final long timeoutMillis,
                                        final String correlationId,
                                        final String signature, final String proposer, final List<String> signers, final long minimumNumberOfSignatures) throws
            BalException {


        if (pendingTransactionsMap.containsKey(correlationId)) {
            throw new InvalidScipParameterException();
        }


        PendingTransaction p = new PendingTransaction();
        p.setBlockchainIdentifier(blockchainIdentifier);
        p.setSmartContractPath(smartContractPath);
        p.setFunctionIdentifier(functionIdentifier);
        p.setInputs(inputs);
        p.setOutputs(outputs);
        p.setSigners(signers);
        p.setCorrelationIdentifier(correlationId);
        p.setMinimumNumberOfSignatures(minimumNumberOfSignatures);
        p.setTypeArguments(typeArguments);
        p.setRequiredConfidence(requiredConfidence);
        p.setCallbackUrl(callbackUrl);
        p.setSignature(signature);
        p.setProposer(proposer);
        p.setTimeoutMillis(timeoutMillis);
        p.setSignatures(new ArrayList<>());

        String invocationHash = generateInvocationHash(blockchainIdentifier, smartContractPath, functionIdentifier,
                typeArguments, inputs, outputs, requiredConfidence, callbackUrl, timeoutMillis, correlationId,
                signature, proposer, signers, minimumNumberOfSignatures);
        p.setInvocationHash(invocationHash);


        pendingTransactionsMap.put(correlationId, p);

    }

    private static String generateInvocationHash(final String blockchainIdentifier,
                                                 final String smartContractPath,
                                                 final String functionIdentifier,
                                                 final List<String> typeArguments,
                                                 final List<Parameter> inputs,
                                                 final List<Parameter> outputs,
                                                 final double requiredConfidence,
                                                 final String callbackUrl,
                                                 final long timeoutMillis,
                                                 final String correlationId,
                                                 final String signature, final String proposer, final List<String> signers, final long minimumNumberOfSignatures) {

        Map<String, Object> map = new HashMap<>();
        map.put("blockchainIdentifier", blockchainIdentifier);
        map.put("smartContractPath", smartContractPath);
        map.put("typeArguments", typeArguments);
        map.put("functionIdentifier", functionIdentifier);
        map.put("inputs", inputs);
        map.put("outputs", outputs);
        map.put("requiredConfidence", requiredConfidence);
        map.put("callbackUrl", callbackUrl);
        map.put("timeoutMillis", timeoutMillis);
        map.put("correlationId", correlationId);
        map.put("signature", signature);
        map.put("signers", signers);
        map.put("proposer", proposer);
        map.put("minimumNumberOfSignatures", minimumNumberOfSignatures);

        try {
            String json = new ObjectMapper().writeValueAsString(map);

            MessageDigest msg = MessageDigest.getInstance("SHA-256");
            byte[] hash = msg.digest(json.getBytes(StandardCharsets.UTF_8));
            // convert bytes to hexadecimal
            StringBuilder s = new StringBuilder();
            for (byte b : hash) {
                s.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            return s.toString();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
