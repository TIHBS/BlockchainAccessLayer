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

import blockchains.iaas.uni.stuttgart.de.model.SmartContractFunctionParameter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ScipParserTest {

    @Test
    void testScipParser() {
        final String scip1 = "scip://eth1/0xdf068aC89E6d5fa88520faace0267047e47102c2/getOwner?:address";
        ScipParser parser = ScipParser.parse(scip1);
        Assertions.assertEquals("eth1", parser.getBlockchainId());
        Assertions.assertEquals("getOwner", parser.getFunctionName());
        Assertions.assertEquals(1, parser.getFunctionPathSegments().length);
        Assertions.assertEquals("0xdf068aC89E6d5fa88520faace0267047e47102c2", parser.getFunctionPathSegments()[0]);
        Assertions.assertEquals(0, parser.getParameterTypes().size());
        Assertions.assertEquals("address", parser.getReturnType());

        final String scip2 = "scip://sawtooth2/seth/0xfa3622e1/set?key=uint&value=uint:void";
        ScipParser parser2 = ScipParser.parse(scip2);
        Assertions.assertEquals("sawtooth2", parser2.getBlockchainId());
        Assertions.assertEquals("set", parser2.getFunctionName());
        Assertions.assertEquals(2, parser2.getFunctionPathSegments().length);
        Assertions.assertEquals("seth", parser2.getFunctionPathSegments()[0]);
        Assertions.assertEquals("0xfa3622e1", parser2.getFunctionPathSegments()[1]);
        Assertions.assertEquals(2, parser2.getParameterTypes().size());
        Assertions.assertEquals(new SmartContractFunctionParameter("key", "uint"), parser2.getParameterTypes().get(0));
        Assertions.assertEquals(new SmartContractFunctionParameter("value", "uint"), parser2.getParameterTypes().get(1));
        Assertions.assertEquals("void", parser2.getReturnType());

        final String scip3 = "scip://eth1/getHeight?:uint";
        ScipParser parser3 = ScipParser.parse(scip3);
        Assertions.assertEquals("eth1", parser3.getBlockchainId());
        Assertions.assertEquals("getHeight", parser3.getFunctionName());
        Assertions.assertEquals(0, parser3.getFunctionPathSegments().length);
        Assertions.assertEquals(0, parser3.getParameterTypes().size());
        Assertions.assertEquals("uint", parser3.getReturnType());
    }
}