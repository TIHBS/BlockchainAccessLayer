package blockchains.iaas.uni.stuttgart.de.management.model;

import blockchains.iaas.uni.stuttgart.de.exceptions.ManualUnsubscriptionException;

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
public class CompletableFutureSubscription<T> extends Subscription {
    private CompletableFuture<T> future;

    public CompletableFutureSubscription(CompletableFuture<T> future, SubscriptionType type) {
        super(type);
        this.future = future;
    }

    public CompletableFuture<T> getFuture() {
        return future;
    }

    public void setFuture(CompletableFuture<T> future) {
        this.future = future;
    }

    @Override
    public void unsubscribe() {
        future.completeExceptionally(new ManualUnsubscriptionException());
    }
}
