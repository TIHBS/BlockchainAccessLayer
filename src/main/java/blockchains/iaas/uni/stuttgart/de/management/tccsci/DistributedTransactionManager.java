package blockchains.iaas.uni.stuttgart.de.management.tccsci;

import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.api.model.*;
import blockchains.iaas.uni.stuttgart.de.management.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.management.model.DistributedTransaction;
import blockchains.iaas.uni.stuttgart.de.management.model.DistributedTransactionState;
import blockchains.iaas.uni.stuttgart.de.management.model.DistributedTransactionVerdict;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DistributedTransactionManager {
    private static final Logger log = LoggerFactory.getLogger(DistributedTransactionManager.class);

    public UUID startDtx() {
        DistributedTransaction tx = new DistributedTransaction();
        DistributedTransactionRepository.getInstance().addDistributedTransaction(tx);
        log.info("Received start_tx request and generated the following id: " + tx.getId());
        return tx.getId();
    }

    public void invokeSc(final String blockchainIdentifier,
                         final String smartContractPath,
                         final String functionIdentifier,
                         final List<Parameter> inputs,
                         final List<Parameter> outputs,
                         final double requiredConfidence,
                         final String callbackUrl,
                         final long timeoutMillis,
                         final String correlationId,
                         final String signature) {
        BlockchainManager manager = new BlockchainManager();
        UUID txId = UUID.fromString(inputs.get(0).getValue());
        log.info("Received invoke_sc request for dtx: " + txId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(txId);

        if (dtx.getState() == DistributedTransactionState.AWAITING_REQUESTS) {
            if (!dtx.getBlockchainIds().contains(blockchainIdentifier)) {
                ResourceManagerSmartContract rmsc = AdapterManager.getInstance().getAdapter(blockchainIdentifier).getResourceManagerSmartContract();
                SmartContractEvent abortEvent = rmsc.getAbortEvent();

                manager.subscribeToEvent(blockchainIdentifier,
                                rmsc.getSmartContractPath(),
                                abortEvent.getFunctionIdentifier(),
                                abortEvent.getOutputs(),
                                0.0,
                                buildEventFilter(abortEvent, dtx.getId()))
                        .take(1)
                        .subscribe(this::handleScError);
                log.info("Subscribed to the abort error of blockchain: " + blockchainIdentifier + " for the dtx: " + dtx.getId());
                dtx.getBlockchainIds().add(blockchainIdentifier);
            }

            manager.invokeSmartContractFunction(blockchainIdentifier, smartContractPath, functionIdentifier, inputs,
                    outputs, requiredConfidence, callbackUrl, timeoutMillis, correlationId, signature);
        }

    }

    public void abortDtx(UUID txId) {
        log.info("Received abort_dtx request for dtx: " + txId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(txId);

        if (dtx.getState() == DistributedTransactionState.AWAITING_REQUESTS) {
            doAbort(txId);
        }
    }

    public void commitDtx(UUID txId) {
        log.info("Received commit_dtx request for dtx: " + txId);
        DistributedTransaction dtx = DistributedTransactionRepository.getInstance().getById(txId);

        if (dtx.getState() == DistributedTransactionState.AWAITING_REQUESTS) {
            dtx.setState(DistributedTransactionState.AWAITING_VOTES);
            dtx.setYes(0);
            BlockchainManager manager = new BlockchainManager();
            List<String> ids = dtx.getBlockchainIds();


            for (String blockchainIdentifier : ids) {
                ResourceManagerSmartContract rmsc = AdapterManager.getInstance().getAdapter(blockchainIdentifier).getResourceManagerSmartContract();
                SmartContractEvent voteEvent = rmsc.getVoteEvent();
                manager.subscribeToEvent(blockchainIdentifier,
                                rmsc.getSmartContractPath(),
                                voteEvent.getFunctionIdentifier(),
                                voteEvent.getOutputs(),
                                0.0,
                                buildEventFilter(voteEvent, dtx.getId()))
                        .take(1)
                        .subscribe(occurrence -> handleVoteEvent(occurrence, dtx, ids.size()));
                log.info("Subscribed to the Vote event of blockchain: " + blockchainIdentifier + " for the dtx: " + dtx.getId());
            }


            CompletableFuture.allOf(ids
                            .stream()
                            .map(bcId -> invokePrepare(bcId, txId))
                            .collect(Collectors.toList())
                            .toArray(new CompletableFuture[ids.size()]))
                    .whenComplete((v, th) -> {
                        log.info("Invoked prepare* of all RMSCs of dtx: " + txId.toString());
                    });
        }
    }

    private static String buildEventFilter(SmartContractEvent abortEvent, UUID txId) {
        String param1Name = abortEvent.getOutputs().get(0).getName();
        return param1Name + "==\"" + txId.toString() + "\"";
    }

    private void handleScError(Occurrence errorDetails) {
        String txIdString = errorDetails.getParameters().get(0).getValue();
        log.info("Received an abort event for dtx: " + txIdString);
        UUID txId = UUID.fromString(txIdString);
        doAbort(txId);
    }

    // todo make synchronized so we do not miss counting votes!
    // todo use a better way to get to the special event arguments (a different blockchain system might have a different order!)
    private void handleVoteEvent(Occurrence voteDetails, DistributedTransaction tx, int bcCount) {
        log.info("Received Vote event for dtx: " + tx.getId().toString());
        boolean isYesVote = Boolean.parseBoolean(voteDetails.getParameters().get(1).getValue());

        if (!isYesVote) {
            doAbort(tx.getId());
        } else {
            tx.setYes(tx.getYes() + 1);

            if (tx.getYes() == bcCount) {
                doCommit(tx.getId());
            }
        }
    }

    private void doAbort(UUID txId) {
        log.info("Aborting transaction: " + txId);
        DistributedTransaction tx = DistributedTransactionRepository.getInstance().getById(txId);
        tx.setVerdict(DistributedTransactionVerdict.ABORT);

        CompletableFuture.allOf(tx.getBlockchainIds()
                        .stream()
                        .map(bcId -> invokeAbort(bcId, txId))
                        .collect(Collectors.toList())
                        .toArray(new CompletableFuture[tx.getBlockchainIds().size()]))
                .whenComplete((v, th) -> {
                    tx.setState(DistributedTransactionState.ABORTED);
                });
    }

    private void doCommit(UUID txId) {
        log.info("Committing transaction: " + txId);
        DistributedTransaction tx = DistributedTransactionRepository.getInstance().getById(txId);
        tx.setVerdict(DistributedTransactionVerdict.COMMIT);

        CompletableFuture.allOf(tx.getBlockchainIds()
                        .stream()
                        .map(bcId -> invokeCommit(bcId, txId))
                        .collect(Collectors.toList())
                        .toArray(new CompletableFuture[tx.getBlockchainIds().size()]))
                .whenComplete((v, th) -> {
                    tx.setState(DistributedTransactionState.COMMITTED);
                });
    }


    private CompletableFuture<Transaction> invokeAbort(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = AdapterManager.getInstance().getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction abortFunction = rmsc.getAbortFunction();
        List<Parameter> functionInputs = abortFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());
        BlockchainManager manager = new BlockchainManager();
        return manager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), abortFunction.getFunctionIdentifier(),
                functionInputs, abortFunction.getOutputs(), 0.0, 0, null);
    }

    private CompletableFuture<Transaction> invokeCommit(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = AdapterManager.getInstance().getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction commitFunction = rmsc.getCommitFunction();
        List<Parameter> functionInputs = commitFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());
        BlockchainManager manager = new BlockchainManager();
        return manager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), commitFunction.getFunctionIdentifier(),
                functionInputs, commitFunction.getOutputs(), 0.0, 0, null);
    }

    private CompletableFuture<Transaction> invokePrepare(String blockchainId, UUID txId) {
        ResourceManagerSmartContract rmsc = AdapterManager.getInstance().getAdapter(blockchainId).getResourceManagerSmartContract();
        SmartContractFunction prepareFunction = rmsc.getPrepareFunction();
        List<Parameter> functionInputs = prepareFunction.getInputs();
        functionInputs.get(0).setValue(txId.toString());
        BlockchainManager manager = new BlockchainManager();
        return manager.invokeSmartContractFunction(blockchainId, rmsc.getSmartContractPath(), prepareFunction.getFunctionIdentifier(),
                functionInputs, prepareFunction.getOutputs(), 0.0, 0, null);
    }


}
