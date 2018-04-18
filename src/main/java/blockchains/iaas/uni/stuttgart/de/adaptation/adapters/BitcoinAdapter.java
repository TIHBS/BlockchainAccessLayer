package blockchains.iaas.uni.stuttgart.de.adaptation.adapters;

import blockchains.iaas.uni.stuttgart.de.adaptation.BlockchainAdapterFactory;
import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.adaptation.utils.BitcoinUtils;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainNodeUnreachableException;
import blockchains.iaas.uni.stuttgart.de.exceptions.InvalidTransactionException;
import blockchains.iaas.uni.stuttgart.de.model.Block;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import com.neemre.btcdcli4j.core.BitcoindException;
import com.neemre.btcdcli4j.core.CommunicationException;
import com.neemre.btcdcli4j.core.client.BtcdClient;
import com.neemre.btcdcli4j.core.domain.PaymentOverview;
import com.neemre.btcdcli4j.core.domain.RawInput;
import com.neemre.btcdcli4j.core.domain.RawTransactionOverview;
import com.neemre.btcdcli4j.daemon.BtcdDaemon;
import com.neemre.btcdcli4j.daemon.event.BlockListener;
import com.neemre.btcdcli4j.daemon.event.WalletListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/********************************************************************************
 * Copyright (c) 2018 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
public class BitcoinAdapter implements BlockchainAdapter {
    private static final Logger log = LoggerFactory.getLogger(BlockchainAdapterFactory.class);
    private BtcdClient client;
    private BtcdDaemon daemon;

    public BitcoinAdapter(BtcdClient client,
                          BtcdDaemon daemon) {
        this.client = client;
        this.daemon = daemon;
    }

    /**
     * Finds the sending address of the first input of a given transaction (requires an indexed bitcoin core)
     *
     * @param transactionId the id of the transaction to inspect
     * @return the Bitcoin address that owned the output used to fund the first input of the given transaction
     * @throws BitcoindException
     * @throws CommunicationException
     */
    private String findTransactionFirstSender(String transactionId) throws BitcoindException, CommunicationException {
        String address = "";
        final RawTransactionOverview rawTx = client.decodeRawTransaction(client.getRawTransaction(transactionId));
        final List<RawInput> vIn = rawTx.getVIn();

        if (vIn.size() > 0) {
            RawInput input = vIn.get(0);
            RawTransactionOverview inputRawTx = client.decodeRawTransaction(client.getRawTransaction(input.getTxId()));
            address = inputRawTx.getVOut().get(input.getVOut()).getScriptPubKey().getAddresses().get(0);
        }

        return address;
    }

    private static Block generateBlockObject(com.neemre.btcdcli4j.core.domain.Block block) {
        final Block result = new Block();
        result.setHash(block.getHash());
        result.setNumberAsLong(block.getHeight().longValue());

        return result;
    }

    private Transaction generateTransactionObject(com.neemre.btcdcli4j.core.domain.Transaction transaction, Block block, boolean detectSender) {
        Transaction result = null;
        // there might be multi-inputs and/or multi-outputs for a transactions, we only consider the first input/output affecting the wallet
        if (transaction.getDetails().size() > 0) {
            final PaymentOverview overview = transaction.getDetails().get(0);
            result = new Transaction();
            result.setTo(overview.getAddress());
            result.setBlock(block);
            result.setTransactionHash(transaction.getTxId());
            result.setValue(BitcoinUtils.bitcoinsToSatoshi(transaction.getAmount().abs()).toBigInteger());// always a positive value!

            if (detectSender) {
                try {
                    result.setFrom(findTransactionFirstSender(result.getTransactionHash()));
                } catch (BitcoindException | CommunicationException e) {
                    final String msg = String.format("Could not detect the sender of the transaction: %s. Reason: %s",
                            result.getTransactionHash(), e.getMessage());
                    log.error(msg);
                }
            }
        }

        return result;
    }


    /**
     * Subscribes for the event of detecting a transition of the state of a given transaction which is assumed to having been
     * MINED before. The method supports detecting
     * (i) a transaction being not found anymore or being in contradiction with another transaction(invalidated),
     * or (ii) not having a containing block (orphaned), or (iii) having received enough block-confirmations(durably committed).
     *
     * @param txHash         the hash of the transaction to monitor
     * @param waitFor        the number of block-confirmations to wait until the transaction is considered persisted (-1 if the
     *                       transaction is never to be considered persisted)
     * @param observedStates the set of states that will be reported to the calling method
     * @return a future which is used to handle the subscription and receive the callback
     */
    private CompletableFuture<Transaction> subscribeForTxEvent(String txHash, long waitFor, TransactionState... observedStates) {
        final CompletableFuture<Transaction> result = new CompletableFuture<>();
        final BlockListener listener = new BlockListener() {
            void handleDetectedState(final com.neemre.btcdcli4j.core.domain.Transaction transactionDetails,
                                     final com.neemre.btcdcli4j.core.domain.Block block,
                                     final TransactionState detectedState, final TransactionState[] interesting,
                                     CompletableFuture<Transaction> future) {
                // Only complete the future if we are interested in this event
                if (Arrays.asList(interesting).contains(detectedState)) {
                    Transaction result = null;

                    if (transactionDetails != null) {
                        final Block myBlock = generateBlockObject(block);
                        result = generateTransactionObject(transactionDetails, myBlock, true);
                    }  else {
                        result = new Transaction();
                    }

                    result.setState(detectedState);
                    future.complete(result);
                }
            }

            @Override
            public void blockDetected(com.neemre.btcdcli4j.core.domain.Block block) {
                try {
                    final com.neemre.btcdcli4j.core.domain.Transaction tx = client.getTransaction(txHash);

                    if (tx == null || tx.getConfirmations() == -1) {// transaction is dropped or specifically found to be in contradiction with another transaction
                        final String msg = String.format("The transaction of the hash %s is not found!", txHash);
                        log.info(msg);
                        handleDetectedState(tx, block, TransactionState.NOT_FOUND, observedStates, result);

                        return;
                    }

                    if (tx.getConfirmations() == 0) {// Not contained in a block anymore
                        final String msg = String.format("The transaction of the hash %s has no block (orphaned?)",
                                txHash);
                        log.info(msg);
                        handleDetectedState(tx, block, TransactionState.PENDING, observedStates, result);
                        return;
                    }

                    // check if enough block-confirmations have occurred.
                    if (waitFor >= 0 && tx.getConfirmations() >= waitFor) {
                        final String msg = String.format("The transaction of the hash %s has been confirmed",
                                txHash);
                        log.info(msg);

                        handleDetectedState(tx, block, TransactionState.CONFIRMED, observedStates, result);
                    }


                } catch (BitcoindException e) {
                    result.completeExceptionally(new InvalidTransactionException(e));
                } catch (CommunicationException e) {
                    result.completeExceptionally(new BlockchainNodeUnreachableException(e));
                }
            }
        };

        // unsubscribe the observable when the CompletableFuture completes (either when detecting an event, or manually)
        result.whenComplete((v, e) -> daemon.removeBlockListener(listener));
        daemon.addBlockListener(listener);

        return result;
    }


    @Override
    public CompletableFuture<Transaction> submitTransaction(long waitFor, String receiverAddress, BigDecimal value) throws InvalidTransactionException {
        try {
            final BigDecimal valueBitcoins = BitcoinUtils.satoshiToBitcoin(value);
            final String transactionId = client.sendToAddress(receiverAddress, valueBitcoins);
            CompletableFuture<Transaction> result;

            if (waitFor > 0) {
                result = subscribeForTxEvent(transactionId, waitFor, TransactionState.NOT_FOUND, TransactionState.CONFIRMED);
            } else {
                result = new CompletableFuture<>();
                final com.neemre.btcdcli4j.core.domain.Transaction tx = client.getTransaction(transactionId);
                final Transaction resultTx = generateTransactionObject(tx, null, true);
                resultTx.setState(TransactionState.CONFIRMED);
                result.complete(resultTx);
            }

            return result;

        } catch (BitcoindException e) {
            throw new InvalidTransactionException(e);
        } catch (CommunicationException e) {
            throw new BlockchainNodeUnreachableException(e);
        }
    }

    @Override
    public Observable<Transaction> receiveTransactions(long waitFor, String senderId) {

        final PublishSubject<Transaction> subject = PublishSubject.create();
        final WalletListener listener = new WalletListener() {
            @Override
            public void walletChanged(com.neemre.btcdcli4j.core.domain.Transaction transaction) {
                // All transactions here are relevant to me as this is a wallet-based event
                // But we need to make sure that the transaction is actually increasing the balance and that this is the
                // first time we see this transaction (we receive multiple notifications for the same transaction)

                if (transaction.getAmount().compareTo(BigDecimal.ZERO) >= 0 && transaction.getConfirmations() == 0) { // a new receive-, or self-transaction
                    try {
                        if (senderId == null || senderId.trim().length() == 0 ||
                                findTransactionFirstSender(transaction.getTxId()).equals(senderId)) {
                            log.info("New transaction received with id " + transaction.getTxId());

                            if (waitFor > 0) {
                                subscribeForTxEvent(transaction.getTxId(), waitFor, TransactionState.CONFIRMED)
                                        .thenAccept(subject::onNext)
                                        .exceptionally(error -> {
                                            subject.onError(error);
                                            return null;
                                        });
                            } else {
                                final Transaction resultTx = generateTransactionObject(transaction, null, true);
                                resultTx.setState(TransactionState.CONFIRMED);
                                subject.onNext(resultTx);
                            }

                        }
                    } catch (BitcoindException | CommunicationException e) {
                        log.error("Failed to receive a Bitcoin transaction. Reason: " + e.getMessage());
                    }
                }

            }
        };

        final Observable<Transaction> result = subject.doOnUnsubscribe(() -> daemon.removeWalletListener(listener));
        daemon.addWalletListener(listener);

        return result;
    }

    @Override
    public CompletableFuture<TransactionState> ensureTransactionState(long waitFor, String transactionId) {
        return subscribeForTxEvent(transactionId, waitFor, TransactionState.NOT_FOUND, TransactionState.CONFIRMED)
                .thenApply(Transaction::getState);
    }

    @Override
    public CompletableFuture<TransactionState> detectOrphanedTransaction(String transactionId) {
        return subscribeForTxEvent(transactionId, -1, TransactionState.PENDING, TransactionState.NOT_FOUND)
                .thenApply(Transaction::getState);
    }

}
