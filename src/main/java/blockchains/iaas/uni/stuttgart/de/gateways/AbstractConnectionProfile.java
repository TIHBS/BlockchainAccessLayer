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

package blockchains.iaas.uni.stuttgart.de.gateways;

import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes( {
        @JsonSubTypes.Type(value = EthereumConnectionProfile.class, name = "ethereum"),
        @JsonSubTypes.Type(value = BitcoinConnectionProfile.class, name = "bitcoin"),
        @JsonSubTypes.Type(value = FabricConnectionProfile.class, name = "fabric")}
)
public abstract class AbstractConnectionProfile {
    private double adversaryVotingRatio;

    public double getAdversaryVotingRatio() {
        return adversaryVotingRatio;
    }

    public void setAdversaryVotingRatio(double adversaryVotingRatio) {
        if (adversaryVotingRatio < 0 || adversaryVotingRatio > 1.0) {
            throw new IllegalArgumentException("Voting power of adversary should be between 0.0 and 1.0, but (" +
                    adversaryVotingRatio + ") is passed!");
        }

        this.adversaryVotingRatio = adversaryVotingRatio;
    }

    public abstract Properties getAsProperties();
}
