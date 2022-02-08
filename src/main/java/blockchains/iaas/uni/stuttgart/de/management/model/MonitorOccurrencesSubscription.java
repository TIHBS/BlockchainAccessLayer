/*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.management.model;

import java.util.List;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import io.reactivex.disposables.Disposable;
import lombok.Data;

@Data
public class MonitorOccurrencesSubscription extends ObservableSubscription {
    private String identifier;
    private List<Parameter> parameters;

    public MonitorOccurrencesSubscription(Disposable subscription, SubscriptionType type, String identifier, List<Parameter> parameters) {
        super(subscription, type);
        this.identifier = identifier;
        this.parameters = parameters;
    }
}
