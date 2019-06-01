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

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.BytesType;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Int;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;

//todo support array types e.g., address[]
public class EthereumReturnValueEncoder {
    public String encodeValue(Type value) throws EthereumParameterEncodingException {
        try {
            if (value instanceof Utf8String || value instanceof Address)
                return value.getValue().toString();

            if (value instanceof Bool)
                return String.valueOf(((Bool) value).getValue());

            if (value instanceof Uint || value instanceof Int)
                return ((BigInteger) value.getValue()).toString(10);

            if (value instanceof DynamicBytes || value instanceof Bytes) {
                if (((BytesType)value).getValue().length > 0) {
                    return (new BigInteger(((BytesType) value).getValue())).toString(16);
                }
                return "empty";
            }
        } catch (Exception e) {
            throw new EthereumParameterEncodingException("An error occurred while encoding return value. Reason: "
                    + e.getMessage(), e);
        }

        throw new EthereumParameterEncodingException("The passed type is not supported! " + value.getTypeAsString());
    }
}
