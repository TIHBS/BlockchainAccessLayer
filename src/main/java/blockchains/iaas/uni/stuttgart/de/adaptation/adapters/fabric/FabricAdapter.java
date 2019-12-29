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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.utils.BooleanExpressionEvaluator;
import blockchains.iaas.uni.stuttgart.de.adaptation.utils.SmartContractPathParser;
import blockchains.iaas.uni.stuttgart.de.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainNodeUnreachableException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidScipParameterException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvokeSmartContractFunctionFailure;
import blockchains.iaas.uni.stuttgart.de.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.exceptions.ParameterException;
import blockchains.iaas.uni.stuttgart.de.model.Occurrence;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.model.QueryResult;
import blockchains.iaas.uni.stuttgart.de.model.TimeFrame;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractEvent;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.impl.event.ContractEventImpl;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.ChaincodeEvent;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Builder
public class FabricAdapter implements BlockchainAdapter {
    private String blockchainId;
    private static final Logger log = LoggerFactory.getLogger(FabricAdapter.class);

    @Override
    public CompletableFuture<Transaction> submitTransaction(String receiverAddress, BigDecimal value, double requiredConfidence

    ) throws InvalidTransactionException, NotSupportedException {
        throw new NotSupportedException("Fabric does not support submitting monetary transactions!");
    }

    @Override
    public Observable<Transaction> receiveTransactions(String senderId, double requiredConfidence) throws NotSupportedException {
        throw new NotSupportedException("Fabric does not support receiving monetary transactions!");
    }

    @Override
    public CompletableFuture<TransactionState> ensureTransactionState(String transactionId, double requiredConfidence) throws NotSupportedException {
        throw new NotSupportedException("Fabric does not support monetary transactions!");
    }

    @Override
    public CompletableFuture<TransactionState> detectOrphanedTransaction(String transactionId) throws NotSupportedException {
        throw new NotSupportedException("Fabric does not support monetary transactions!");
    }

    @Override
    public CompletableFuture<Transaction> invokeSmartContract(
            String smartContractPath,
            String functionIdentifier,
            List<Parameter> inputs,
            List<Parameter> outputs,
            double requiredConfidence) throws BalException {
        if (outputs.size() > 1) {
            throw new ParameterException("Hyperledger Fabric supports only at most a single return value.");
        }
        CompletableFuture<Transaction> result = new CompletableFuture<>();
        SmartContractPathElements path = this.parsePathElements(smartContractPath);

        try {
            Contract contract = GatewayManager.getInstance().getContract(blockchainId, path.channel, path.chaincode);
            String[] params = inputs.stream().map(Parameter::getValue).toArray(String[]::new);

            try {
                byte[] resultAsBytes = contract.submitTransaction(functionIdentifier, params);
                Transaction resultT = new Transaction();

                if (outputs.size() == 1) {
                    Parameter resultP = Parameter
                            .builder()
                            .name(outputs.get(0).getName())
                            .value(new String(resultAsBytes, StandardCharsets.UTF_8))
                            .build();
                    resultT.setReturnValues(Collections.singletonList(resultP));
                    log.info(resultP.getValue());
                } else if (outputs.size() == 0) {
                    log.info("Fabric transaction without a return value executed!");
                    resultT.setReturnValues(Collections.emptyList());
                }

                resultT.setState(TransactionState.RETURN_VALUE);
                result.complete(resultT);
            } catch (Exception e) {
                // exceptions at this level are invocation exceptions. They should be sent asynchronously to the client app.
                result.completeExceptionally(new InvokeSmartContractFunctionFailure(e.getMessage()));
            }
        } catch (Exception e) {
            // this is a synchronous exception.
            throw new BlockchainNodeUnreachableException(e.getMessage());
        }

        return result;
    }

    @Override
    public Observable<Occurrence> subscribeToEvent(
            String smartContractAddress,
            String eventIdentifier,
            List<Parameter> outputParameters,
            double degreeOfConfidence,
            String filter) throws BalException {
        SmartContractPathElements path = this.parsePathElements(smartContractAddress);
        Contract contract = GatewayManager.getInstance().getContract(blockchainId, path.channel, path.chaincode);
        final PublishSubject<Occurrence> result = PublishSubject.create();

        Consumer<ContractEvent> consumer = contract.addContractListener(event -> {
            log.info(event.toString());

            try {
                Occurrence occurrence = this.handleEvent(event, outputParameters, filter);

                if (occurrence != null) {
                    result.onNext(occurrence);
                }
            } catch (InvalidScipParameterException e) {
                result.onError(e);
            }
        }, eventIdentifier);

        return result.doFinally(() -> contract.removeContractListener(consumer));
    }

    @Override
    public CompletableFuture<QueryResult> queryEvents(String smartContractAddress, String eventIdentifier, List<Parameter> outputParameters, String filter, TimeFrame timeFrame) throws BalException {
        try {
            final SmartContractPathElements path = this.parsePathElements(smartContractAddress);
            final LocalDateTime fromDateTime = timeFrame.getFromLocalDateTime();
            final LocalDateTime toDateTime = timeFrame.getToLocalDateTime();
            final Network network = GatewayManager.getInstance().getChannel(blockchainId, path.channel);
            final long latestBlockNumber = network
                    .getChannel()
                    .queryBlockchainInfo()
                    .getHeight() - 1;
            final CompletableFuture<QueryResult> result = new CompletableFuture<>();
            final QueryResult queryResult = QueryResult.builder().occurrences(new ArrayList<>()).build();
            log.info("latest Fabric block number: {}", latestBlockNumber);

            // the listening is over either when the block number is past the latest block number at the time of invocation,
            // or when the "to" timestamp is exceeded by the transaction timestamp.
            // using the provided ContractListener does not work since we cannot tell when past events are done in the replay!
            final Consumer<BlockEvent> consumer = network.addBlockListener(0, blockEvent -> {
                log.info("handling block no. {}", blockEvent.getBlockNumber());

                blockEvent.getTransactionEvents().forEach(tE -> {
                    log.info("handling transaction hash: {}", tE.getTransactionID());
                    final LocalDateTime transactionDateTime = tE.getTimestamp().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    // we could be already done!
                    if (toDateTime != null && toDateTime.isBefore(transactionDateTime)) {
                        log.info("transaction after last allowed time. Completing!");
                        result.complete(queryResult);
                    } else {
                        // ensure we are not before the first permitted timestamp
                        if (fromDateTime == null || fromDateTime.isBefore(transactionDateTime)) {
                            // iterate over events of this transaction
                            tE.getTransactionActionInfos().forEach(tAI -> {
                                ChaincodeEvent cE = tAI.getEvent();
                                // check if name matches
                                if (cE.getEventName().equals(eventIdentifier)) {
                                    // check if filter evaluates to true
                                    Occurrence occurrence = this.handleEvent(new ContractEventImpl(tE, cE), outputParameters, filter);

                                    if (occurrence != null) {
                                        // we found a matching occurrence
                                        queryResult.getOccurrences().add(occurrence);
                                    }
                                }
                            });
                        }
                    }
                });

                if (blockEvent.getBlockNumber() >= latestBlockNumber) {
                    log.info("currentBlock >= latestBlock so completing!");
                    result.complete(queryResult);
                }
            });

            return result.whenComplete((res, error) -> network.removeBlockListener(consumer));
        } catch (ProposalException | InvalidArgumentException e) {
            throw new BlockchainNodeUnreachableException(e.getMessage());
        }
    }

    private Occurrence handleEvent(ContractEvent event, List<Parameter> outputParameters, String filter) throws InvalidScipParameterException {
        // todo try to parse the returned value according to the outputParameters
        List<Parameter> parameters = new ArrayList<>();

        if (event.getPayload().isPresent() && outputParameters.size() > 0) {
            Parameter parameter = Parameter
                    .builder()
                    .name(outputParameters.get(0).getName())
                    .type(outputParameters.get(0).getType())
                    .value(new String(event.getPayload().get(), StandardCharsets.UTF_8))
                    .build();
            parameters.add(parameter);
        }

        try {
            if (BooleanExpressionEvaluator.evaluate(filter, parameters)) {
                return Occurrence
                        .builder()
                        .parameters(parameters)
                        .isoTimestamp(DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.of("UTC")).format(event.getTransactionEvent().getTimestamp().toInstant()))
                        .build();
            }

            return null;
        } catch (Exception e) {
            throw new InvalidScipParameterException(e.getMessage());
        }
    }

    @Override
    public String testConnection() {
        try {
            Gateway gateway = GatewayManager.getInstance().getGateway(blockchainId);
            if (gateway.getIdentity() != null)
                return "true";
            else
                return "Cannot get gateway identity!";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private SmartContractPathElements parsePathElements(String smartContractPath) throws InvokeSmartContractFunctionFailure {
        SmartContractPathParser parser = SmartContractPathParser.parse(smartContractPath);
        String[] pathSegments = parser.getSmartContractPathSegments();

        if (pathSegments.length != 3 && pathSegments.length != 2) {
            String message = String.format("Unable to identify the path to the requested function. Expected path segments: 3 or 2. Found path segments: %s", pathSegments.length);
            log.error(message);
            throw new InvalidScipParameterException(message);
        }

        SmartContractPathElements.SmartContractPathElementsBuilder builder = SmartContractPathElements
                .builder()
                .channel(pathSegments[0])
                .chaincode(pathSegments[1]);

        if (pathSegments.length == 3) {
            builder = builder.smartContract(pathSegments[2]);
        }

        return builder.build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class SmartContractPathElements {
        private String channel;
        private String chaincode;
        private String smartContract;
    }
}
