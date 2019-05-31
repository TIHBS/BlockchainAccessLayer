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


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.web3j.abi.datatypes.Type;

import static org.junit.jupiter.api.Assertions.*;

class EthereumParameterDecoderTest {

    @Test
    void decodeParameter() throws EthereumParameterDecodingException {
        // only "happy" tests for now
        // todo add more sophisticated tests!
        EthereumParameterDecoder decoder = new EthereumParameterDecoder();
        String[][] cases = {
                {"string", "I am going crazy!!"},
                {"address", "0xacf6BadFa47BC05DB7C39Ba3Cd27003a3fD7E0c9"},
                {"bool", "True", "bool"},
                {"bytes", "00112233445566778899aabbccddeeff"},
                {"bytes2", "00ff"},
                {"bytes32", "00ff00ff00ff00ff00ff00ff00ff00ff00ff00ff00ff00ff00ff00ff00ff00ff"},
                {"int", "-1234566"},
                {"int8", "-1"},
                {"uint", "1234566"},
                {"uint8", "1"}
        };

        for (String[] c:cases) {
            String expectedValue = c[0];

            if (expectedValue.equals("int") || expectedValue.equals("uint"))
                expectedValue += "256";

            Assertions.assertEquals(expectedValue, decoder.decodeParameter(c[0], c[1]).getTypeAsString());
        }

    }
}