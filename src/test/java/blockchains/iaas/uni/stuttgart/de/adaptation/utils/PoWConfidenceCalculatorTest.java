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

import blockchains.iaas.uni.stuttgart.de.model.Block;
import blockchains.iaas.uni.stuttgart.de.model.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PoWConfidenceCalculatorTest {

    @Test
    void getCurrentConfidence() {
        final double ETH_EXPECTED_CONFIDENCE = 0.99970567;
        final PoWConfidenceCalculator calculator = new PoWConfidenceCalculator();
        final Block blockEth = new Block();
        blockEth.setNumberAsLong(1);
        final Transaction transactionEth = new Transaction();
        transactionEth.setBlock(blockEth);
        calculator.setCurrentBlockchainHeight(13);
        calculator.setAdversaryRatio(0.2);
        double result = calculator.getCurrentConfidence(transactionEth);
        Assertions.assertTrue( MathUtils.doubleEquals(result, ETH_EXPECTED_CONFIDENCE));
    }
}