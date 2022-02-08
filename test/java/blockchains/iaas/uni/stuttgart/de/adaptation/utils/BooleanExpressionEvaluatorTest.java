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

import java.util.Collections;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.api.utils.BooleanExpressionEvaluator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanExpressionEvaluatorTest {

    @Test
    void testEvaluateExpressionWithStringVariable() throws Exception {
        final String intType = "{ \"type\": \"integer\"} ";
        final String expression = "newValue <= 500";
        final Parameter parameter = Parameter.builder()
                .name("newValue")
                .type(intType)
                .value("499")
                .build();
        assertTrue(BooleanExpressionEvaluator.evaluate(expression, Collections.singletonList(parameter)));
        parameter.setValue("501");
        assertFalse(BooleanExpressionEvaluator.evaluate(expression, Collections.singletonList(parameter)));
    }
}