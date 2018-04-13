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
import com.neemre.btcdcli4j.core.domain.enums.PaymentCategories;
import com.neemre.btcdcli4j.daemon.BtcdDaemon;
import com.neemre.btcdcli4j.daemon.event.BlockListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.math.BigDecimal;
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
     * @param transactionId the id of the transaction to inspect
     * @return the Bitcoin address that owned the output used to fund the first input of the given transaction
     * @throws BitcoindException
     * @throws CommunicationException
     */
    private String findTransactionFirstSender(String transactionId) throws BitcoindException, CommunicationException {
        String address = "";
        final RawTransactionOverview rawTx = client.decodeRawTransaction(client.getRawTransaction(transactionId));
        final List<RawInput> vIn = rawTx.getVIn();

        if(vIn.size() > 0){
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
        if(transaction.getDetails().size() > 0){
            final PaymentOverview overview = transaction.getDetails().get(0);
            result = new Transaction();
            result.setTo(overview.getAddress());
            result.setBlock(block);
            result.setTransactionHash(transaction.getTxId());
            result.setValue(BitcoinUtils.bitcoinsToSatoshi(transaction.getAmount().abs()).toBigInteger());// always a positive value!

            if(detectSender) {
                try {
                    result.setFrom(findTransactionFirstSender(result.getTransactionHash()));
                } catch (BitcoindException|CommunicationException e) {
                    final String msg = String.format("Could not detect the sender of the transaction: %s. Reason: %s",
                            result.getTransactionHash(), e.getMessage());
                    log.error(msg);
                }
            }
        }

        return result;
    }


    @Override
    public CompletableFuture<Transaction> submitTransaction(long waitFor, String receiverAddress, BigDecimal value) throws InvalidTransactionException {
        try {
            final BigDecimal valueBitcoins = BitcoinUtils.satoshiToBitcoin(value);
            final String transactionId = client.sendToAddress(receiverAddress, valueBitcoins);
            final CompletableFuture<Transaction> result = new CompletableFuture<>();

            if (waitFor > 0) {
                final BlockListener listener = new BlockListener() {
                    @Override
                    public void blockDetected(com.neemre.btcdcli4j.core.domain.Block block) {
                        try {
                            final com.neemre.btcdcli4j.core.domain.Transaction tx = client.getTransaction(transactionId);

                            if (tx.getConfirmations().longValue() >= 0) {
                                if (tx.getConfirmations().longValue() >= waitFor) {
                                    log.info("transaction with id: " + transactionId + " received all required confirmations!");
                                    final Block myBlock = generateBlockObject(block);
                                    final Transaction resultTx = generateTransactionObject(tx, myBlock, true);
                                    resultTx.setState(TransactionState.CONFIRMED);
                                    result.complete(resultTx);

                                }

                            } else {// -1 means a conflicted transaction
                                log.error("Conflicted Bitcoin transaction detected after submission.");
                                result.completeExceptionally(new InvalidTransactionException());
                            }

                        } catch (BitcoindException e) {
                            result.completeExceptionally(new InvalidTransactionException(e));
                        } catch (CommunicationException e) {
                            result.completeExceptionally(new BlockchainNodeUnreachableException(e));
                        }
                    }
                };

                result.whenComplete((tx, e) -> {
                    daemon.removeBlockListener(listener);
                });
                daemon.addBlockListener(listener);
            } else {
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
        return null;
    }

    @Override
    public CompletableFuture<TransactionState> ensureTransactionState(long waitFor, String transactionId) {
        return null;
    }

    @Override
    public CompletableFuture<TransactionState> detectOrphanedTransaction(String transactionId) {
        return null;
    }

    @Override
    public boolean doesTransactionExist(String transactionId) throws IOException {
        return false;
    }
}
