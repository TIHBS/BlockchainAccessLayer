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
import blockchains.iaas.uni.stuttgart.de.model.LinearChainTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PoWConfidenceCalculatorTest {
    private final long DEPTH = 12;
    private final double Q = 0.2;
    @Test
    void getCurrentConfidence() {

        final PoWConfidenceCalculator calculator = new PoWConfidenceCalculator();
        final Block blockEth = new Block();
        blockEth.setNumberAsLong(1);
        final LinearChainTransaction transactionEth = new LinearChainTransaction();
        transactionEth.setBlock(blockEth);
        calculator.setCurrentBlockchainHeight(1 + DEPTH);
        calculator.setAdversaryRatio(Q);
        double result = calculator.getCurrentConfidence(transactionEth);
        double ETH_EXPECTED_CONFIDENCE = 0.99970567;
        Assertions.assertTrue( MathUtils.doubleEquals(result, ETH_EXPECTED_CONFIDENCE));
    }

    @Test
    void getEquivalentDepth() {
        final PoWConfidenceCalculator calculator = new PoWConfidenceCalculator();
        calculator.setAdversaryRatio(Q);
        Assertions.assertEquals(DEPTH, calculator.getEquivalentBlockDepth(0.999));
    }
}