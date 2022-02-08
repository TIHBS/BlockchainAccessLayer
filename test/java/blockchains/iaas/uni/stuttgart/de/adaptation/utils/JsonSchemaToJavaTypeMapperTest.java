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

import javax.xml.bind.DatatypeConverter;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import blockchains.iaas.uni.stuttgart.de.api.utils.JsonSchemaToJavaTypeMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonSchemaToJavaTypeMapperTest {

    @Test
    void test() {
        Parameter parameter = new Parameter();

        final String stringType = "{\n" +
                "\t\"type\": \"string\"\n" +
                "}";
        parameter.setType(stringType);
        parameter.setValue("hello world!");
        Object result = JsonSchemaToJavaTypeMapper.map(parameter);
        Assertions.assertEquals(String.class, result.getClass());
        Assertions.assertEquals("hello world!", result);

        final String addressType = "{\n" +
                "\t\"type\": \"string\",\n" +
                "\t\"pattern\": \"^0x[a-fA-F0-9]{40}$\"\n" +
                "}";
        parameter.setType(addressType);
        parameter.setValue("0x52908400098527886E0F7030069857D2E4169EE7");
        result = JsonSchemaToJavaTypeMapper.map(parameter);
        Assertions.assertEquals(String.class, result.getClass());
        Assertions.assertEquals("0x52908400098527886E0F7030069857D2E4169EE7".toLowerCase(), result.toString().toLowerCase());
        final String uintType = "{\n" +
                "\t\"type\": \"integer\"\n" +
                "}";
        parameter.setType(uintType);
        parameter.setValue("-1");
        result = JsonSchemaToJavaTypeMapper.map(parameter);
        Assertions.assertEquals(Long.class, result.getClass());
        Assertions.assertEquals(-1L, result);

        final String bytes = "{\n" +
                "\t\"type\": \"array\",\n" +
                "\t\"items\": {\n" +
                "\t\t\"type\": \"string\",\n" +
                "\t\t\"pattern\": \"^[a-fA-F0-9]{2}$\"\n" +
                "\t}\n" +
                "}";
        parameter.setType(bytes);
        parameter.setValue("[aa,bb,cc,dd,11,22,33,44]");
        result = JsonSchemaToJavaTypeMapper.map(parameter);
        Assertions.assertEquals(Object[].class, result.getClass());
        byte[] resultAsArray = new byte[((Object[]) result).length];

        for (int i = 0; i < resultAsArray.length; i++) {
            resultAsArray[i] = (Byte)((Object[]) result)[i];
        }

        Assertions.assertArrayEquals(DatatypeConverter.parseHexBinary("aabbccdd11223344")
                , resultAsArray);

        final String bool = "{\"type\": \"boolean\"}";
        parameter.setType(bool);
        parameter.setValue("true");
        result = JsonSchemaToJavaTypeMapper.map(parameter);
        Assertions.assertEquals(Boolean.class, result.getClass());
        Assertions.assertEquals(true, result);
    }
}