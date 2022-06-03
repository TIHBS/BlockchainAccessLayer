/********************************************************************************
 * Copyright (c) 2019-2022 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package blockchains.iaas.uni.stuttgart.de.subscription;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import blockchains.iaas.uni.stuttgart.de.subscription.model.MonitorOccurrencesSubscription;
import blockchains.iaas.uni.stuttgart.de.subscription.model.Subscription;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionKey;
import blockchains.iaas.uni.stuttgart.de.subscription.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo this class should persist subscriptions to stable storage!
// todo rethink subscription logic!
public class SubscriptionManager {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);
    private static SubscriptionManager instance = null;
    private Map<SubscriptionKey, Subscription> subscriptions = Collections.synchronizedMap(new HashMap<>());

    private SubscriptionManager() {

    }

    public static SubscriptionManager getInstance() {
        if (instance == null) {
            instance = new SubscriptionManager();
        }

        return instance;
    }

    public void createSubscription(String subscriptionId, String blockchainId, String smartContractPath, Subscription subscription) {
        SubscriptionKey key = SubscriptionKey.builder().smartContractPath(smartContractPath).blockchainId(blockchainId).correlationId(subscriptionId).build();
        if (this.subscriptions.containsKey(key)) {
            log.error("Subscription key <{}> already exists! Not subscribing!", key);
            // to not keep any loose ends
            Subscription old = this.subscriptions.get(key);
            old.unsubscribe();
        } else {
            this.subscriptions.put(key, subscription);
        }
    }

    public void createSubscription(String subscriptionId, String blockchainId, Subscription subscription) {
        this.createSubscription(subscriptionId, blockchainId, "", subscription);
    }

    public Subscription getSubscription(String subscriptionId, String blockchainId, String smartContractPath) {
        SubscriptionKey key = SubscriptionKey.builder().smartContractPath(smartContractPath).blockchainId(blockchainId).correlationId(subscriptionId).build();
        if (this.subscriptions.containsKey(key)) {
            return this.subscriptions.get(key);
        } else {
            log.info("trying to retrieve a non-existent subscription: <{}>! null is returned", key);
            return null;
        }
    }

    public void removeSubscription(String subscriptionId, String blockchainId, String smartContractPath) {
        SubscriptionKey key = SubscriptionKey.builder().smartContractPath(smartContractPath).blockchainId(blockchainId).correlationId(subscriptionId).build();
        if (this.subscriptions.containsKey(key)) {
            this.subscriptions.remove(key);
        } else {
            log.info("trying to remove a non-existent subscription: <{}>! nothing is removed", subscriptionId);
        }
    }

    public void removeSubscription(String subscriptionId, String blockchainId) {
        this.removeSubscription(subscriptionId, blockchainId, "");
    }

    public Collection<SubscriptionKey> getAllSubscriptionKeysOfType(SubscriptionType type) {
        return this.subscriptions
                .keySet()
                .stream()
                .filter(subscriptionKey -> this.subscriptions.get(subscriptionKey).getType() == type)
                .collect(Collectors.toList());
    }

    public Collection<SubscriptionKey> getAllSubscriptionIdsOfFunction(String blockchainId, String smartContractPath, String id, List<Parameter> inputs) {
        return this.getAllSubscriptionIdsOfIdentifiable(blockchainId, smartContractPath, id, inputs, SubscriptionType.FUNCTION_INVOCATIONS);
    }

    public Collection<SubscriptionKey> getAllSubscriptionIdsOfEvent(String blockchainId, String smartContractPath, String id, List<Parameter> inputs) {
        return this.getAllSubscriptionIdsOfIdentifiable(blockchainId, smartContractPath, id, inputs, SubscriptionType.EVENT_OCCURRENCES);
    }

    Map<SubscriptionKey, Subscription> getAllSubscriptions() {
        return this.subscriptions;
    }

    private Collection<SubscriptionKey> getAllSubscriptionIdsOfIdentifiable(String blockchainId, String smartContractPath, String id, List<Parameter> inputs, SubscriptionType type) {
        return this.subscriptions
                .keySet()
                .stream()
                .filter(subscriptionKey -> {
                            if (subscriptionKey.getBlockchainId().equals(blockchainId)
                                    && subscriptionKey.getSmartContractPath().equals(smartContractPath)
                                    && this.subscriptions.get(subscriptionKey) instanceof MonitorOccurrencesSubscription) {
                                MonitorOccurrencesSubscription subscription = (MonitorOccurrencesSubscription) this.subscriptions.get(subscriptionKey);
                                if (subscription.getType().equals(type)) {
                                    if (id == null || inputs == null)
                                        return true;
                                    if (subscription.getIdentifier().equals(id)) {
                                        for (int i = 0; i < inputs.size(); i++) {
                                            if (!subscription.getParameters().get(i).getType().equals(inputs.get(i).getType())) {
                                                return false;
                                            }
                                        }

                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                )
                .collect(Collectors.toList());
    }
}
