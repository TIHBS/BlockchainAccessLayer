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

package blockchains.iaas.uni.stuttgart.de.api.connectionprofiles;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Properties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public abstract class AbstractConnectionProfile {
    private static final String PREFIX = "common.";
    private static final String ADVERSARY_VOTING_RATIO = PREFIX + "adversaryVotingRatio";
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

    public Properties getAsProperties() {
        Properties result = new Properties();
        result.put(ADVERSARY_VOTING_RATIO, String.valueOf(adversaryVotingRatio));

        return  result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractConnectionProfile that = (AbstractConnectionProfile) o;
        return this.getAsProperties().equals(that.getAsProperties());
    }

    @Override
    public int hashCode() {
        return this.getAsProperties().hashCode();
    }
}
