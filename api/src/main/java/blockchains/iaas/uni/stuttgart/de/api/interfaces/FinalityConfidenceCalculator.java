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
package blockchains.iaas.uni.stuttgart.de.api.interfaces;

import blockchains.iaas.uni.stuttgart.de.api.model.LinearChainTransaction;

/**
 * Calculates the DoC of a transaction
 */
public interface FinalityConfidenceCalculator {
    double getCurrentConfidence(LinearChainTransaction transaction);
}
