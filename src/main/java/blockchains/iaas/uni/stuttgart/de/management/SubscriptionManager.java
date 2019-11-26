/********************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package blockchains.iaas.uni.stuttgart.de.management;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import blockchains.iaas.uni.stuttgart.de.management.model.MonitorOccurrencesSubscription;
import blockchains.iaas.uni.stuttgart.de.management.model.Subscription;
import blockchains.iaas.uni.stuttgart.de.management.model.SubscriptionType;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// todo this class should persist subscriptions to stable storage!
public class SubscriptionManager {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionManager.class);
    private static SubscriptionManager instance = null;
    private Map<String, Subscription> subscriptions = Collections.synchronizedMap(new HashMap<>());

    private SubscriptionManager() {

    }

    public static SubscriptionManager getInstance() {
        if (instance == null) {
            instance = new SubscriptionManager();
        }

        return instance;
    }

    public void createSubscription(String subscriptionId, Subscription subscription) {
        if (this.subscriptions.containsKey(subscriptionId)) {
            log.error("subscription-id <{}> already exists!", subscriptionId);
        } else {
            this.subscriptions.put(subscriptionId, subscription);
        }
    }

    public Subscription getSubscription(String subscriptionId) {
        if (this.subscriptions.containsKey(subscriptionId)) {
            return this.subscriptions.get(subscriptionId);
        } else {
            log.info("trying to retrieve a non-existent subscription: <{}>! null is returned", subscriptionId);
            return null;
        }
    }

    public void removeSubscription(String subscriptionId) {
        if (this.subscriptions.containsKey(subscriptionId)) {
            this.subscriptions.remove(subscriptionId);
        } else {
            log.info("trying to remove a non-existent subscription: <{}>! nothing is removed", subscriptionId);
        }
    }

    public Collection<String> getAllSubscriptionIdsOfType(SubscriptionType type) {
        return this.subscriptions
                .keySet()
                .stream()
                .filter(subscriptionId -> this.subscriptions.get(subscriptionId).getType() == type)
                .collect(Collectors.toList());
    }

    public Collection<String> getAllSubscriptionIdsOfFunction(String id, List<Parameter> inputs) {
        return this.getAllSubscriptionIdsOfIdentifiable(id, inputs, SubscriptionType.FUNCTION_INVOCATIONS);
    }

    public Collection<String> getAllSubscriptionIdsOfEvent(String id, List<Parameter> inputs) {
        return this.getAllSubscriptionIdsOfIdentifiable(id, inputs, SubscriptionType.EVENT_OCCURRENCES);
    }

    private Collection<String> getAllSubscriptionIdsOfIdentifiable(String id, List<Parameter> inputs, SubscriptionType type) {
        return this.subscriptions
                .keySet()
                .stream()
                .filter(subscriptionId -> {
                            MonitorOccurrencesSubscription subscription = (MonitorOccurrencesSubscription) this.subscriptions.get(subscriptionId);
                            if (subscription.getType().equals(type)) {
                                if (subscription.getIdentifier().equals(id)) {
                                    for (int i = 0; i < inputs.size(); i++) {
                                        if (!subscription.getParameters().get(i).getType().equals(inputs.get(i).getType())) {
                                            return false;
                                        }
                                    }

                                    return true;
                                }
                            }
                            return false;
                        }
                )
                .collect(Collectors.toList());
    }
}
