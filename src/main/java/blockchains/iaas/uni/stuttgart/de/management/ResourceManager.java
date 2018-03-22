package blockchains.iaas.uni.stuttgart.de.management;


import blockchains.iaas.uni.stuttgart.de.model.TransactionState;
import blockchains.iaas.uni.stuttgart.de.adaptation.AdapterManager;
import blockchains.iaas.uni.stuttgart.de.exceptions.BlockchainIdNotFoundException;
import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.BlockchainAdapter;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import blockchains.iaas.uni.stuttgart.de.exceptions.SubmitTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/********************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 ********************************************************************************/
public class ResourceManager {
    private static final Logger log = LoggerFactory.getLogger(ResourceManager.class);

    public void submitNewTransaction(final String subscriptionId, final String to, final BigInteger value,
                                     final String blockchainId, final long waitFor, final Long timeout, final String epUrl) {
        final AdapterManager adapterManager = AdapterManager.getInstance();

        try {
            final BlockchainAdapter adapter = adapterManager.getAdapter(blockchainId);
            final long timeoutVal = timeout == null ? -1 : timeout.intValue();
            final CompletableFuture<Transaction> future = adapter.submitTransaction(waitFor, timeoutVal, to, new BigDecimal(value));
            // This (should only) happen when something is wrong with the communication with the blockchain node
            future.exceptionally((e) -> {
                if(e.getCause() instanceof IOException)
                    CallbackManager.getInstance().sendCallback(epUrl, subscriptionId, TransactionState.UNKNOWN);
                else if(e.getCause() instanceof RuntimeException)
                    CallbackManager.getInstance().sendCallback(epUrl, subscriptionId, TransactionState.INVALID);
                return null;
            });
            future.thenAccept(tx -> {
                if (tx != null) {
                    CallbackManager.getInstance().sendCallback(epUrl, subscriptionId, tx);
                } else
                    log.info("resulting transaction is null");

            });
            // we do not provide the ability to manually unsubscribe from this subscription, so we do not store the
            // subscription in the first place
            //final Subscription subscription = new CompletableFutureSubscription<>(future);
            //SubscriptionManager.getInstance().createSubscription(subscriptionId, subscription);
        } catch (SubmitTransactionException e) {
            // This (should only) happen when something is wrong with the transaction data
            CallbackManager.getInstance().sendCallbackAsync(epUrl, subscriptionId, TransactionState.INVALID);

        } catch (BlockchainIdNotFoundException e) {
            // This (should only) happen when the blockchainId is not found
            CallbackManager.getInstance().sendCallbackAsync(epUrl, subscriptionId, TransactionState.UNKNOWN);
        }

    }
}
