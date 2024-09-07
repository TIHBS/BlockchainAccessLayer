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
package blockchains.iaas.uni.stuttgart.de.management.model;

import java.util.concurrent.CompletableFuture;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.ManualUnsubscriptionException;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CompletableFutureSubscription<T> extends Subscription {
    private CompletableFuture<T> future;

    public CompletableFutureSubscription(CompletableFuture<T> future, SubscriptionType type) {
        super(type);
        this.future = future;
    }

    @Override
    public void unsubscribe() {
        if (future != null) {
            future.completeExceptionally(new ManualUnsubscriptionException());
        }
    }
}
