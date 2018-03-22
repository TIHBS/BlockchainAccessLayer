package blockchains.iaas.uni.stuttgart.de.management;

import blockchains.iaas.uni.stuttgart.de.management.model.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
            log.error("subscription-id <{}> already exists!");
        } else {
            this.subscriptions.put(subscriptionId, subscription);
        }
    }

    public Subscription getSubscription(String subscriptionId){
        if(this.subscriptions.containsKey(subscriptionId)){
            return this.subscriptions.get(subscriptionId);
        }else{
            log.error("subscription-id <{}> does not exist! null is returned");
            return null;
        }
    }

}
