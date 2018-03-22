package blockchains.iaas.uni.stuttgart.de.management.model;

import blockchains.iaas.uni.stuttgart.de.exceptions.ManualUnsubscriptionException;

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
public class CompletableFutureSubscription<T> extends Subscription {
    private CompletableFuture<T> future;

    public CompletableFutureSubscription(CompletableFuture<T> future) {
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
