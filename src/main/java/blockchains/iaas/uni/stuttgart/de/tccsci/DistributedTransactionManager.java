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
import blockchains.iaas.uni.stuttgart.de.api.exceptions.NotSupportedException;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.UnknownException;
import blockchains.iaas.uni.stuttgart.de.api.model.*;
import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.connectionprofiles.ConnectionProfilesManager;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransaction;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionState;
import blockchains.iaas.uni.stuttgart.de.tccsci.model.DistributedTransactionVerdict;
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
        String param1Name = abortEvent.getOutputs().get(0).getName();
        return param1Name + "==\"" + txId.toString() + "\"";
    }

    public String registerBc(final UUID dtxId, final String blockchainIdentifier) {
        log.info("Received register_bc({}) request for dtx: {}", blockchainIdentifier, dtxId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(dtxId);

        if (dtx.getState() == DistributedTransactionState.STARTED) {
            if (!dtx.getBlockchainIds().contains(blockchainIdentifier)) {
                ResourceManagerSmartContract rmsc = this.adapterManager.getAdapter(blockchainIdentifier).getResourceManagerSmartContract();
                SmartContractEvent abortEvent = rmsc.getAbortEvent();

                this.blockchainManager.subscribeToEvent(blockchainIdentifier,
                                rmsc.getSmartContractPath(),
                                abortEvent.getFunctionIdentifier(),
                                abortEvent.getOutputs(),
                                0.0,
                                buildEventFilter(abortEvent, dtxId))
                        .take(1)
                        .subscribe(this::handleScError);
                log.info("Subscribed to the abort error of blockchain: {} for the dtx: {}", blockchainIdentifier, dtxId);
                dtx.getBlockchainIds().add(blockchainIdentifier);
            }

            final String identity = getBlockchainIdentity(blockchainIdentifier);
            log.debug("Reporting the identity to the client: {}", identity);

            return identity;
        }

        throw new NotSupportedException("The requested operation requires the current transaction to be in the STARTED state, instead: " + dtx.getState());
    }

    public UUID startDtx() {
        DistributedTransaction tx = new DistributedTransaction();
        DistributedTransactionRepository.getInstance().addDistributedTransaction(tx);
        final UUID transactionId = tx.getId();
        log.info("Received start_tx request and generated the following id: {}", () -> transactionId);
        return transactionId;
    }

    public void abortDtx(UUID txId) {
        log.info("Received abort_dtx request for dtx: {}", txId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(txId);

        if (dtx.getState() == DistributedTransactionState.STARTED) {
            doAbort(txId);
        }
    }

    public void commitDtx(UUID txId) {
        log.info("Received commit_dtx request for dtx: {}", txId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(txId);

        if (dtx.getState() == DistributedTransactionState.STARTED) {
            dtx.setState(DistributedTransactionState.AWAITING_VOTES);
            dtx.setYes(0);
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
                        .subscribe(occurrence -> handleVoteEvent(occurrence, dtx, ids.size()));
                log.info("Subscribed to the Vote event of blockchain: {} for the dtx: {}", blockchainIdentifier, dtxId);
            }


            CompletableFuture.allOf(ids
                            .stream()
                            .map(bcId -> invokePrepare(bcId, txId))
                            .toList()
                            .toArray(new CompletableFuture[ids.size()]))
                    .whenComplete((v, th) -> {
                        log.info("Invoked prepare* of all RMSCs of dtx: {}", txId);
                    });
        }
    }

    protected String getBlockchainIdentity(String blockchainId) {
        return ConnectionProfilesManager.getInstance().getConnectionProfiles().get(blockchainId).getIdentity();
    }

    private void handleScError(Occurrence errorDetails) {
        String txIdString = errorDetails.getParameters().get(0).getValue();
        log.info("Received an abort event for dtx: {}", txIdString);
        UUID txId = UUID.fromString(txIdString);
        doAbort(txId);
    }

    // todo make synchronized so we do not miss counting votes!
    // todo use a better way to get to the special event arguments (a different blockchain system might have a different order!)
    private void handleVoteEvent(Occurrence voteDetails, DistributedTransaction tx, int bcCount) {
        final UUID txId = tx.getId();
        log.info("Received Vote event for dtx: {}", txId);
        boolean isYesVote = Boolean.parseBoolean(voteDetails.getParameters().get(1).getValue());

        if (!isYesVote) {
            doAbort(txId);
        } else {
            tx.setYes(tx.getYes() + 1);

            if (tx.getYes() == bcCount) {
                doCommit(txId);
            }
        }
    }

    private void doAbort(UUID txId) {
        log.info("Aborting transaction: {}", txId);
        DistributedTransaction tx = DistributedTransactionRepository.getInstance().getById(txId);
        tx.setVerdict(DistributedTransactionVerdict.ABORT);

        CompletableFuture.allOf(tx.getBlockchainIds()
                        .stream()
                        .map(bcId -> invokeAbort(bcId, txId))
                        .toList()
                        .toArray(new CompletableFuture[tx.getBlockchainIds().size()]))
                .whenComplete((v, th) -> {
                    tx.setState(DistributedTransactionState.ABORTED);
                });
    }

    private void doCommit(UUID txId) {
        log.info("Committing transaction: {}", txId);
        DistributedTransaction tx = DistributedTransactionRepository.getInstance().getById(txId);
        tx.setVerdict(DistributedTransactionVerdict.COMMIT);

        CompletableFuture.allOf(tx.getBlockchainIds()
                        .stream()
                        .map(bcId -> invokeCommit(bcId, txId))
                        .toList()
                        .toArray(new CompletableFuture[tx.getBlockchainIds().size()]))
                .whenComplete((v, th) -> {
                    tx.setState(DistributedTransactionState.COMMITTED);
                });
    }


    private CompletableFuture<Transaction> invokeAbort(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = adapterManager.getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction abortFunction = rmsc.getAbortFunction();
        List<Parameter> functionInputs = abortFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());

        return blockchainManager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), abortFunction.getFunctionIdentifier(),
                functionInputs, abortFunction.getOutputs(), 0.0, 0, null, true);
    }

    private CompletableFuture<Transaction> invokeCommit(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = adapterManager.getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction commitFunction = rmsc.getCommitFunction();
        List<Parameter> functionInputs = commitFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());

        return blockchainManager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), commitFunction.getFunctionIdentifier(),
                functionInputs, commitFunction.getOutputs(), 0.0, 0, null, true);
    }

    private CompletableFuture<Transaction> invokePrepare(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = adapterManager.getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction prepareFunction = rmsc.getPrepareFunction();
        List<Parameter> functionInputs = prepareFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());

        return blockchainManager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), prepareFunction.getFunctionIdentifier(),
                functionInputs, prepareFunction.getOutputs(), 0.0, 0, null, true);
    }


}
