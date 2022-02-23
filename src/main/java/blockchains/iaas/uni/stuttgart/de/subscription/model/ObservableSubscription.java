/********************************************************************************
 * Copyright (c) 2018-2019 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.subscription.model;

import io.reactivex.disposables.Disposable;

public class ObservableSubscription extends Subscription {
    private Disposable subscription;

    public ObservableSubscription(Disposable subscription, SubscriptionType type) {
        super(type);
        this.subscription = subscription;
    }

    public Disposable getSubscription() {
        return subscription;
    }

    public void setSubscription(Disposable subscription) {
        this.subscription = subscription;
    }

    public void unsubscribe() {
        this.subscription.dispose();
    }
}
