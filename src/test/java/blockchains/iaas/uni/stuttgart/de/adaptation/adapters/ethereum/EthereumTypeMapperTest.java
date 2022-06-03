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

package blockchains.iaas.uni.stuttgart.de.adaptation.adapters.ethereum;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes8;
import org.web3j.abi.datatypes.generated.Int160;
import org.web3j.abi.datatypes.generated.Uint160;

import static org.junit.jupiter.api.Assertions.*;
// 2^255 = 57896044618658097711785492504343953926634992332820282019728792003956564819968
// 2^256 = 115792089237316195423570985008687907853269984665640564039457584007913129639935
class EthereumTypeMapperTest {
    @Test
    void testTypes(){
        final String stringType = "{\n" +
                "\t\"type\": \"string\"\n" +
                "}";
        Class<? extends Type> result = EthereumTypeMapper.getEthereumType(stringType);
        Assertions.assertEquals(Utf8String.class, result);
        final String addressType = "{\n" +
                "\t\"type\": \"string\",\n" +
                "\t\"pattern\": \"^0x[a-fA-F0-9]{40}$\"\n" +
                "}";
        result = EthereumTypeMapper.getEthereumType(addressType);
        Assertions.assertEquals(Address.class, result);
        final String uint160Type = "{\n" +
                "\t\"type\": \"integer\",\n" +
                " \t\"minimum\": 0,\n" +
                " \t\"maximum\": 1461501637330902918203684832716283019655932542975\n" +
                "}";
        result = EthereumTypeMapper.getEthereumType(uint160Type);

        Assertions.assertEquals(Uint160.class, result);

        final String int160Type = "{\n" +
                "\t\"type\": \"integer\",\n" +
                " \t\"minimum\": -730750818665451459101842416358141509827966271488,\n" +
                " \t\"maximum\": 730750818665451459101842416358141509827966271487\n" +
                "}";
        result = EthereumTypeMapper.getEthereumType(int160Type);
        Assertions.assertEquals(Int160.class, result);

        final String bytes8 = "{\n" +
                "\t\"type\": \"array\",\n" +
                "\t\"maxItems\": 8,\n" +
                "\t\"items\": {\n" +
                "\t\t\"type\": \"string\",\n" +
                "\t\t\"pattern\": \"^[a-fA-F0-9]{2}$\"\n" +
                "\t}\n" +
                "}";
        result = EthereumTypeMapper.getEthereumType(bytes8);
        Assertions.assertEquals(Bytes8.class, result);

        final String bytes = "{\n" +
                "\t\"type\": \"array\",\n" +
                "\t\"items\": {\n" +
                "\t\t\"type\": \"string\",\n" +
                "\t\t\"pattern\": \"^[a-fA-F0-9]{2}$\"\n" +
                "\t}\n" +
                "}";
        result = EthereumTypeMapper.getEthereumType(bytes);
        Assertions.assertEquals(DynamicBytes.class, result);
    }

}