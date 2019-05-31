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

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;

/**
 * Byte array values are expected to be a single hexadecimal number.
 * Uint values are expected to be decimal numbers
 * Int values are expected to be decimal numbers
 */
public class EthereumParameterDecoder {
    class FixedLengthByteArrayType extends Bytes {

        FixedLengthByteArrayType(int byteSize, byte[] value) {
            super(byteSize, value);
        }
    }

    class FixedLengthUintType extends Uint {

        FixedLengthUintType(int bitSize, BigInteger value) {
            super(bitSize, value);
        }
    }

    class FixedLengthIntType extends Int {

        FixedLengthIntType(int bitSize, BigInteger value) {
            super(bitSize, value);
        }
    }

    public Type decodeParameter(String typeFull, String value) throws EthereumParameterDecodingException {
        Pattern pattern = Pattern.compile("^([a-zA-Z]+)([0-9]*)(\\[\\])?$");
        Matcher matcher = pattern.matcher(typeFull);

        if (matcher.matches()) {
            String type = matcher.group(1);
            String lengthS = matcher.group(2);
            String bracketsS = matcher.group(3);

            if (bracketsS != null && !bracketsS.isEmpty()) {
                throw new EthereumParameterDecodingException("Arrays with brackets [] are not yet supported by the adapter");
            }

            int length = -1;

            if (lengthS != null && !lengthS.isEmpty()) {
                length = Integer.parseInt(lengthS);

                if (length == 0) {
                    throw new EthereumParameterDecodingException("Type length cannot be zero!");
                }
            }

            try {
                switch (type) {
                    case "address":
                        return new Address(value);
                    case "bool":
                        return new Bool(Boolean.parseBoolean(value));
                    case "string":
                        return new Utf8String(value);
                    case "bytes":
                        return handleByteArrayType(value, length);
                    case "uint":
                        return handleUintType(value, length);
                    case "int":
                        return handleIntType(value, length);
                }
            } catch (NumberFormatException e) {
                throw new EthereumParameterDecodingException("Cannot convert the value to a numeric format: " + value, e);
            }
        }

        throw new EthereumParameterDecodingException("Unsupported type encountered: "
                + typeFull);
    }

    private Type handleUintType(String value, int length) throws EthereumParameterDecodingException {
        BigInteger uintValAsBigInteger = new BigInteger(value, 10);
        if (length < 0) {
            return new Uint(uintValAsBigInteger);
        }

        // 8,16,24,..,256 are allowed
        if (length > 256 || length % 8 != 0) {
            throw new EthereumParameterDecodingException(
                    "Fixed-size uints should the following length: 8,16,24,..,256");
        }

        if (length < uintValAsBigInteger.bitLength()) {
            throw new EthereumParameterDecodingException(
                    "The value does not match the type!");
        }

        return new FixedLengthUintType(length, uintValAsBigInteger);
    }

    private Type handleIntType(String value, int length) throws EthereumParameterDecodingException {
        BigInteger intValAsBigInteger = new BigInteger(value, 10);

        if (length < 0) {
            return new Int(intValAsBigInteger);
        }

        // 8,16,24,..,256 are allowed
        if (length > 256 || length % 8 != 0) {
            throw new EthereumParameterDecodingException(
                    "Fixed-size ints should the following length: 8,16,24,..,256");
        }

        if (length < intValAsBigInteger.bitLength()) {
            throw new EthereumParameterDecodingException(
                    "The value does not match the type!");
        }

        return new FixedLengthIntType(length, intValAsBigInteger);
    }

    private Type handleByteArrayType(String value, int length) throws EthereumParameterDecodingException {
        byte[] valueAsBytes = new BigInteger(value, 16).toByteArray();

        if (length < 0) {
            return new DynamicBytes(valueAsBytes);
        }

        if (length > 32) {
            throw new EthereumParameterDecodingException("Fixed-size byte arrays cannot exceed 32 in length!");
        }

        return new FixedLengthByteArrayType(length, valueAsBytes);
    }
}
