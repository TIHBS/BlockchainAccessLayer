/********************************************************************************
 * Copyright (c) 2018-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.controllers;

import blockchains.iaas.uni.stuttgart.de.BlockchainManager;
import blockchains.iaas.uni.stuttgart.de.subscription.SubscriptionManager;
import blockchains.iaas.uni.stuttgart.de.subscription.model.Subscription;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionKey;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;


public abstract class SubscriptionController {
    protected final BlockchainManager manager;

    protected SubscriptionController(BlockchainManager manager) {
        this.manager = manager;
    }

    Collection<SubscriptionKey> getSubscriptions(final SubscriptionType type) {
        final SubscriptionManager manager = SubscriptionManager.getInstance();

        return manager.getAllSubscriptionKeysOfType(type);
    }

    private Subscription getSubscription(final String subscriptionId, final String blockchainId, final String smartContractPath) {
        final SubscriptionManager manager = SubscriptionManager.getInstance();
        return manager.getSubscription(subscriptionId, blockchainId, smartContractPath);
    }

    void removeSubscription(final String subscriptionId, final String blockchainId, final String smartContractPath) {
        final SubscriptionManager manager = SubscriptionManager.getInstance();
        final Subscription subscription = manager.getSubscription(subscriptionId, blockchainId, smartContractPath);

        if (subscription != null) {
            subscription.unsubscribe();
            // removing the subscription from the list is done elsewhere (in the BlockchainManager)
        }
    }

    @DeleteMapping(path = "/{subscriptionId}")
    public void removeSubscriptionOperation(@PathVariable("subscriptionId") final String subscriptionId,
                                            @RequestParam("blockchain-id") final String blockchainId,
                                            @RequestParam(value = "address", required = false) final String smartContractPath) {
        this.removeSubscription(subscriptionId, blockchainId, smartContractPath == null ? "" : smartContractPath);
    }

    @GetMapping(path = "/{subscriptionId}")
    public Subscription getSubscriptionDetails(@PathVariable("subscriptionId") final String subscriptionId,
                                               @RequestParam("blockchain-id") final String blockchainId,
                                               @RequestParam(value = "address", required = false) final String smartContractPath) {

        return this.getSubscription(subscriptionId, blockchainId, smartContractPath == null ? "" : smartContractPath);
    }
}
