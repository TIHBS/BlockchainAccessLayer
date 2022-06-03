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

package blockchains.iaas.uni.stuttgart.de.subscription.model;

import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionKey {
    private String correlationId;
    private String blockchainId;
    private String smartContractPath;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionKey that = (SubscriptionKey) o;
        return correlationId.equals(that.correlationId) &&
                blockchainId.equals(that.blockchainId) &&
                smartContractPath.equals(that.smartContractPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, blockchainId, smartContractPath);
    }

    @Override
    public String toString() {
        return String.format("BC-Id: %s, Corr-Id: %s, SC-Path: %s", blockchainId, correlationId, smartContractPath);
    }
}
