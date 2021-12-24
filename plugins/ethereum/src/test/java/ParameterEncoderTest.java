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

import javax.xml.bind.DatatypeConverter;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import blockchains.iaas.uni.stuttgart.demo.ParameterEncoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes8;
import org.web3j.abi.datatypes.generated.Int160;
import org.web3j.abi.datatypes.generated.Uint160;

class ParameterEncoderTest {
    @Test
    void testParameterEncoding() {
        Parameter parameter = new Parameter();

        final String stringType = "{\n" +
                "\t\"type\": \"string\"\n" +
                "}";
        parameter.setType(stringType);
        parameter.setValue("hello world!");
        Type result = ParameterEncoder.encode(parameter);
        Assertions.assertEquals(Utf8String.class, result.getClass());
        Assertions.assertEquals("hello world!", ((Utf8String) result).getValue());

        final String addressType = "{\n" +
                "\t\"type\": \"string\",\n" +
                "\t\"pattern\": \"^0x[a-fA-F0-9]{40}$\"\n" +
                "}";
        parameter.setType(addressType);
        parameter.setValue("0x52908400098527886E0F7030069857D2E4169EE7");
        result = ParameterEncoder.encode(parameter);
        Assertions.assertEquals(Address.class, result.getClass());
        Assertions.assertEquals("0x52908400098527886E0F7030069857D2E4169EE7".toLowerCase(), ((Address) result).getValue());

        final String uint160Type = "{\n" +
                "\t\"type\": \"integer\",\n" +
                " \t\"minimum\": 0,\n" +
                " \t\"maximum\": 1461501637330902918203684832716283019655932542975\n" +
                "}";
        parameter.setType(uint160Type);
        parameter.setValue("1");
        result = ParameterEncoder.encode(parameter);
        Assertions.assertEquals(Uint160.class, result.getClass());
        Assertions.assertEquals("1", ((Uint160) result).getValue().toString(10));

        final String int160Type = "{\n" +
                "\t\"type\": \"integer\",\n" +
                " \t\"minimum\": -730750818665451459101842416358141509827966271488,\n" +
                " \t\"maximum\": 730750818665451459101842416358141509827966271487\n" +
                "}";
        parameter.setType(int160Type);
        parameter.setValue("-1");
        result = ParameterEncoder.encode(parameter);
        Assertions.assertEquals(Int160.class, result.getClass());
        Assertions.assertEquals("-1", ((Int160) result).getValue().toString(10));

        final String bytes8 = "{\n" +
                "\t\"type\": \"array\",\n" +
                "\t\"maxItems\": 8,\n" +
                "\t\"items\": {\n" +
                "\t\t\"type\": \"string\",\n" +
                "\t\t\"pattern\": \"^[a-fA-F0-9]{2}$\"\n" +
                "\t}\n" +
                "}";
        parameter.setType(bytes8);
        parameter.setValue("aabbccdd11223344");
        result = ParameterEncoder.encode(parameter);
        Assertions.assertEquals(Bytes8.class, result.getClass());
        Assertions.assertArrayEquals(DatatypeConverter.parseHexBinary("aabbccdd11223344"), ((Bytes8) result).getValue());

        final String bytes = "{\n" +
                "\t\"type\": \"array\",\n" +
                "\t\"items\": {\n" +
                "\t\t\"type\": \"string\",\n" +
                "\t\t\"pattern\": \"^[a-fA-F0-9]{2}$\"\n" +
                "\t}\n" +
                "}";
        parameter.setType(bytes);
        parameter.setValue("aabbccdd11223344");
        result = ParameterEncoder.encode(parameter);
        Assertions.assertEquals(DynamicBytes.class, result.getClass());
        Assertions.assertArrayEquals(DatatypeConverter.parseHexBinary("aabbccdd11223344"), ((DynamicBytes) result).getValue());

        final String bool = "{\"type\": \"boolean\"}";
        parameter.setType(bool);
        parameter.setValue("true");
        result = ParameterEncoder.encode(parameter);
        Assertions.assertEquals(Bool.class, result.getClass());
        Assertions.assertEquals(true, ((Bool) result).getValue());
    }
}