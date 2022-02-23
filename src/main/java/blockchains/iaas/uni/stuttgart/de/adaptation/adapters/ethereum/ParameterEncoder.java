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

import java.math.BigInteger;

import javax.xml.bind.DatatypeConverter;

import blockchains.iaas.uni.stuttgart.de.scip.model.exceptions.ParameterException;
import blockchains.iaas.uni.stuttgart.de.model.Parameter;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;

public class ParameterEncoder {
    public static Type encode(Parameter parameter) throws ParameterException {
        Class<? extends Type> typeClass = EthereumTypeMapper.getEthereumType(parameter.getType());
        try {
            if (typeClass == Bool.class) {
                return new Bool(Boolean.parseBoolean(parameter.getValue()));
            }

            if (typeClass == Address.class) {
                return new Address(parameter.getValue());
            }

            if (typeClass == Utf8String.class) {
                return new Utf8String(parameter.getValue());
            }

            if (typeClass == DynamicBytes.class) {
                return new DynamicBytes(DatatypeConverter.parseHexBinary(parameter.getValue()));
            }

            if (typeClass.getSuperclass() == Int.class) {
                return typeClass.getDeclaredConstructor(BigInteger.class)
                        .newInstance(new BigInteger(parameter.getValue(), 10));
            }

            if (typeClass.getSuperclass() == Uint.class) {
                return typeClass.getDeclaredConstructor(BigInteger.class)
                        .newInstance(new BigInteger(parameter.getValue(), 10));
            }

            if (typeClass.getSuperclass() == Bytes.class) {
                byte[] value = DatatypeConverter.parseHexBinary(parameter.getValue());
                return typeClass.getDeclaredConstructor(value.getClass())
                        .newInstance((Object) value);
            }

            throw new ParameterException("Unrecognized parameter type!");
        } catch (Exception e) {
            if (e instanceof ParameterException)
                throw (ParameterException) e;

            throw new ParameterException(e.getMessage());
        }
    }
}
