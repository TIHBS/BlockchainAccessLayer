/********************************************************************************
 * Copyright (c) 2023-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package blockchains.iaas.uni.stuttgart.de.tccsci;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.ManualUnsubscriptionException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.UnknownException;
import blockchains.iaas.uni.stuttgart.de.api.model.*;
import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.callback.CallbackRouter;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.AsynchronousBalException;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransaction;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionState;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionVerdict;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.exception.IllegalProtocolStateException;
import io.reactivex.disposables.Disposable;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
public class DistributedTransactionManager {
    private final AdapterManager adapterManager;
    private final BlockchainManager blockchainManager;

    public DistributedTransactionManager(AdapterManager adapterManager, BlockchainManager blockchainManager) {
        this.adapterManager = adapterManager;
        this.blockchainManager = blockchainManager;
    }

    private static String buildEventFilter(SmartContractEvent abortEvent, UUID txId) {
        String param2Name = abortEvent.getOutputs().get(1).getName();
        return param2Name + "==\"" + txId.toString() + "\"";
    }

    public String isAbortedInBc(final UUID  dtxId, final String blockchainIdentifier) {
        ResourceManagerSmartContract rmsc = this.adapterManager.getAdapter(blockchainIdentifier).getResourceManagerSmartContract();
        SmartContractEvent abortEvent = rmsc.getAbortEvent();
        QueryResult result = this.blockchainManager.queryEvents(blockchainIdentifier, rmsc.getSmartContractPath(), abortEvent.getFunctionIdentifier(), abortEvent.getOutputs(), "1==1", null);

        return String.valueOf(result.getOccurrences().stream().anyMatch(occurrence -> occurrence.getParameters().get(1).getValue().equals(dtxId.toString())));
    }

    public String registerBc(final UUID dtxId, final String blockchainIdentifier) throws IllegalProtocolStateException {
        log.info("Received register_bc({}) request for dtx: {}", blockchainIdentifier, dtxId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(dtxId);

        if (dtx.getState() == DistributedTransactionState.STARTED) {
            if (!dtx.getBlockchainIds().contains(blockchainIdentifier)) {
//                ResourceManagerSmartContract rmsc = this.adapterManager.getAdapter(blockchainIdentifier).getResourceManagerSmartContract();
//                SmartContractEvent abortEvent = rmsc.getAbortEvent();
//
//                this.blockchainManager.subscribeToEvent(blockchainIdentifier,
//                                rmsc.getSmartContractPath(),
//                                abortEvent.getFunctionIdentifier(),
//                                abortEvent.getOutputs(),
//                                0.0,
//                                buildEventFilter(abortEvent, dtxId))
//                        .take(1)
//                        .subscribe(this::handleScError);
//                log.info("Subscribed to the abort error of blockchain: {} for the dtx: {}", blockchainIdentifier, dtxId);
                dtx.getBlockchainIds().add(blockchainIdentifier);
            }

            final String identity = getBlockchainIdentity(blockchainIdentifier);
            log.debug("Reporting the identity to the client: {}", identity);

            return identity;
        }

        throw new IllegalProtocolStateException("The requested operation requires the current transaction to be in the STARTED state, instead: " + dtx.getState());
    }

    public UUID startDtx() {
        DistributedTransaction tx = new DistributedTransaction();
        DistributedTransactionRepository.getInstance().addDistributedTransaction(tx);
        final UUID transactionId = tx.getId();
        log.info("Received start_tx request and generated the following id: {}", () -> transactionId);
        return transactionId;
    }

    public void abortDtx(UUID txId, String callbackUrl) {
        log.info("Received abort_dtx request for dtx: {}", txId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(txId);

        if (dtx.getState() == DistributedTransactionState.STARTED) {
            doAbort(txId, false, callbackUrl);
        } else {
            throw new IllegalProtocolStateException("The requested operation requires the current transaction to be in the STARTED state, instead: " + dtx.getState());
        }
    }

    public void commitDtx(UUID txId, String callbackUrl) {
        log.info("Received commit_dtx request for dtx: {}", txId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(txId);

        if (dtx.getState() == DistributedTransactionState.STARTED) {
            dtx.setState(DistributedTransactionState.AWAITING_VOTES);
            List<String> ids = dtx.getBlockchainIds();

            for (String blockchainIdentifier : ids) {
                ResourceManagerSmartContract rmsc = adapterManager.getAdapter(blockchainIdentifier).getResourceManagerSmartContract();
                SmartContractEvent voteEvent = rmsc.getVoteEvent();
                final UUID dtxId = dtx.getId();
                blockchainManager.subscribeToEvent(blockchainIdentifier,
                                rmsc.getSmartContractPath(),
                                voteEvent.getFunctionIdentifier(),
                                voteEvent.getOutputs(),
                                0.0,
                                buildEventFilter(voteEvent, dtxId))
                        .take(1)
                        .subscribe(occurrence -> {
                            handleVoteEvent(occurrence, dtx, ids.size(), callbackUrl);
                        });
                log.info("Subscribed to the Vote event of blockchain: {} for the dtx: {}", blockchainIdentifier, dtxId);
            }

            CompletableFuture.allOf(ids
                            .stream()
                            .map(bcId -> invokePrepare(bcId, txId))
                            .toList()
                            .toArray(new CompletableFuture[ids.size()]))
                    .exceptionally(e -> {
                        log.error("Error detected while invoking prepare* of all RMSCs of dtx: {}", txId, e);

                        if (callbackUrl != null && e.getCause() instanceof BalException) {
                            AsynchronousBalException exception =
                                    new AsynchronousBalException((BalException) e.getCause(), txId.toString());
                            CallbackRouter.getInstance().sendAsyncError(txId.toString(), callbackUrl, "json-rpc", TransactionState.UNKNOWN, exception);
                        }

                        // todo must return callback
                        if (e instanceof ManualUnsubscriptionException || e.getCause() instanceof ManualUnsubscriptionException) {
                            log.info("Manual unsubscription of SC invocation!");
                        }

                        return null;
                    })
                    .whenComplete((v, th) -> {
                        log.info("Invoked prepare* of all RMSCs of dtx: {}", txId);
                    });
        } else {
            throw new IllegalProtocolStateException("The requested operation requires the current transaction to be in the STARTED state, instead: " + dtx.getState());
        }
    }

    protected String getBlockchainIdentity(String blockchainId) {
        return ConnectionProfilesManager.getInstance().getConnectionProfiles().get(blockchainId).getIdentity();
    }

    private void handleScError(Occurrence errorDetails) {
        String txIdString = errorDetails.getParameters().get(0).getValue();
        log.info("Received an abort event for dtx: {}", txIdString);
        UUID txId = UUID.fromString(txIdString);
        doAbort(txId, false, null);
    }

    // todo make synchronized so we do not miss counting votes!
    // todo use a better way to get to the special event arguments (a different blockchain system might have a different order!)
    private void handleVoteEvent(Occurrence voteDetails, DistributedTransaction tx, int bcCount, String callbackUrl) {
        final UUID txId = tx.getId();
        log.info("Received Vote event for dtx: {}", txId);
        boolean isYesVote = Boolean.parseBoolean(voteDetails.getParameters().get(2).getValue());

        if (!isYesVote) {
            doAbort(txId, true, callbackUrl);
        } else {
            if (tx.getYes().incrementAndGet() == bcCount) {
                doCommit(txId, callbackUrl);
            }
        }
    }

    private void doAbort(UUID txId, boolean isUserCommit, String callbackUrl) {
        log.info("Aborting transaction: {}", txId);
        DistributedTransaction tx = DistributedTransactionRepository.getInstance().getById(txId);
        tx.setVerdict(DistributedTransactionVerdict.ABORT);

        CompletableFuture.allOf(tx.getBlockchainIds()
                        .stream()
                        .map(bcId -> invokeAbort(bcId, txId))
                        .toList()
                        .toArray(new CompletableFuture[tx.getBlockchainIds().size()]))
                .exceptionally(e -> {
                    log.error("Error detected while invoking abort* of all RMSCs of dtx: {}", txId, e);

                    if (callbackUrl != null && e.getCause() instanceof BalException) {
                        AsynchronousBalException exception =
                                new AsynchronousBalException((BalException) e.getCause(), txId.toString());
                        CallbackRouter.getInstance().sendAsyncError(txId.toString(), callbackUrl, "json-rpc", TransactionState.UNKNOWN, exception);
                    }

                    // todo must return callback
                    if (e instanceof ManualUnsubscriptionException || e.getCause() instanceof ManualUnsubscriptionException) {
                        log.info("Manual unsubscription of SC invocation!");
                    }

                    return null;
                })
                .whenComplete((v, th) -> {
                    log.info("Invoked abort* of all RMSCs of dtx: {}", txId);
                    tx.setState(DistributedTransactionState.ABORTED);
                    if (callbackUrl != null) {
                        if (isUserCommit) {
                            CallbackRouter.getInstance().sendCommitResponse(callbackUrl, tx, false);
                        } else {
                            CallbackRouter.getInstance().sendAbortResponse(callbackUrl, tx);
                        }
                    }
                });
    }

    private void doCommit(UUID txId, String callbackUrl) {
        log.info("Committing transaction: {}", txId);
        DistributedTransaction tx = DistributedTransactionRepository.getInstance().getById(txId);
        tx.setVerdict(DistributedTransactionVerdict.COMMIT);

        CompletableFuture.allOf(tx.getBlockchainIds()
                        .stream()
                        .map(bcId -> invokeCommit(bcId, txId))
                        .toList()
                        .toArray(new CompletableFuture[tx.getBlockchainIds().size()]))
                .exceptionally(e -> {
                    log.error("Error detected while invoking commit* of all RMSCs of dtx: {}", txId, e);

                    if (callbackUrl != null && e.getCause() instanceof BalException) {
                        AsynchronousBalException exception =
                                new AsynchronousBalException((BalException) e.getCause(), txId.toString());
                        CallbackRouter.getInstance().sendAsyncError(txId.toString(), callbackUrl, "json-rpc", TransactionState.UNKNOWN, exception);
                    }

                    // todo must return callback
                    if (e instanceof ManualUnsubscriptionException || e.getCause() instanceof ManualUnsubscriptionException) {
                        log.info("Manual unsubscription of SC invocation!");
                    }

                    return null;
                })
                .whenComplete((v, th) -> {
                    log.info("Invoked commit* of all RMSCs of dtx: {}", txId);
                    tx.setState(DistributedTransactionState.COMMITTED);

                    if (callbackUrl != null) {
                        CallbackRouter.getInstance().sendCommitResponse(callbackUrl, tx, true);
                    }
                });
    }


    private CompletableFuture<Transaction> invokeAbort(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = adapterManager.getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction abortFunction = rmsc.getAbortFunction();
        List<Parameter> functionInputs = abortFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());

        return blockchainManager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), abortFunction.getFunctionIdentifier(),
                functionInputs, abortFunction.getOutputs(), 0.0, 100000, null, true);
    }

    private CompletableFuture<Transaction> invokeCommit(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = adapterManager.getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction commitFunction = rmsc.getCommitFunction();
        List<Parameter> functionInputs = commitFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());

        return blockchainManager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), commitFunction.getFunctionIdentifier(),
                functionInputs, commitFunction.getOutputs(), 0.0, 100000, null, true);
    }

    private CompletableFuture<Transaction> invokePrepare(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = adapterManager.getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction prepareFunction = rmsc.getPrepareFunction();
        List<Parameter> functionInputs = prepareFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());

        return blockchainManager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), prepareFunction.getFunctionIdentifier(),
                functionInputs, prepareFunction.getOutputs(), 0.0, 100000, null, true);
    }


}
