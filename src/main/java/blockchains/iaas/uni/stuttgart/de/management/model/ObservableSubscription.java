package blockchains.iaas.uni.stuttgart.de.management.model;



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
public class ObservableSubscription extends  Subscription {
    private rx.Subscription subscription;

    public ObservableSubscription(rx.Subscription subscription, SubscriptionType type) {
        super(type);
        this.subscription = subscription;
    }

    public rx.Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(rx.Subscription subscription) {
        this.subscription = subscription;
    }

    public void unsubscribe() {
        this.subscription.unsubscribe();
    }
}
