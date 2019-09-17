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
package blockchains.iaas.uni.stuttgart.de.adaptation.utils;

import blockchains.iaas.uni.stuttgart.de.adaptation.interfaces.FinalityConfidenceCalculator;
import blockchains.iaas.uni.stuttgart.de.model.LinearChainTransaction;

public class BFTConfidenceCalculator implements FinalityConfidenceCalculator {
    @Override
    public double getCurrentConfidence(LinearChainTransaction transaction) {
        // always return 1.0 since when BFT consensus succeeds, we are sure that the included tx are durably committed.
        return 1.0;
    }
}
