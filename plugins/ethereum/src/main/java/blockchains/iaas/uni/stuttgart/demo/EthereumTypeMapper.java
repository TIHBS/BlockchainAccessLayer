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

package blockchains.iaas.uni.stuttgart.demo;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;

import javax.json.Json;
import javax.json.JsonObject;

import blockchains.iaas.uni.stuttgart.de.api.utils.MathUtils;
import blockchains.iaas.uni.stuttgart.de.api.exceptions.ParameterException;
import org.web3j.abi.datatypes.AbiTypes;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;

public class EthereumTypeMapper {
    public static Class<? extends Type> getEthereumType(String typeAsJsonSchema) throws ParameterException {
        JsonObject jsonObject = null;
        try {
            jsonObject = Json.createReader(new ByteArrayInputStream(typeAsJsonSchema.getBytes())).readObject();

            String type = jsonObject.getString("type");

            if (type.equals("boolean")) {
                return Bool.class;
            }

            if (type.equals("string")) {
                return handleStringType(jsonObject);
            }

            if (type.equals("integer")) {
                return handleIntegerType(jsonObject);
            }

            if (type.equals("array")) {
                return handleArrayType(jsonObject);
            }

            throw new ParameterException("Unrecognized type " + type);
        } catch (Exception e) {
            throw new ParameterException(e.getMessage());
        }
    }

    private static Class<? extends Type> handleStringType(JsonObject jsonObject) {
        if (jsonObject.containsKey("pattern")) {
            if (jsonObject.getString("pattern").equals("^0x[a-fA-F0-9]{40}$")) {
                return Address.class;
            } else {
                throw new ParameterException("Unrecognized string type");
            }
        }

        return Utf8String.class;
    }

    private static Class<? extends Type> handleIntegerType(JsonObject jsonObject) throws ArithmeticException {
        if (jsonObject.containsKey("minimum") && jsonObject.containsKey("maximum")) {
            BigInteger minimum = jsonObject.getJsonNumber("minimum").bigIntegerValue();
            BigInteger maximum = jsonObject.getJsonNumber("maximum").bigIntegerValue();

            if (minimum.equals(BigInteger.ZERO)) {
                // this might be a uint<M>. Let's try to find M
                if (maximum.compareTo(BigInteger.ZERO) > 0) {
                    // will throw an exception if not exact!
                    int m = MathUtils.log2(maximum.add(BigInteger.ONE));

                    if (m % 8 == 0) {
                        return AbiTypes.getType("uint" + m);
                    }
                }
            } else {
                if (minimum.compareTo(BigInteger.ZERO) < 0 && minimum.abs().equals(maximum.add(BigInteger.ONE))) {
                    // this might be an int<M>. Let's try to find M
                    // will throw an exception if not exact!
                    int m = MathUtils.log2(maximum.add(BigInteger.ONE));

                    if ((m + 1) % 8 == 0) {
                        return AbiTypes.getType("int" + (m + 1));
                    }
                }
            }
        }

        throw new ParameterException("Unrecognized integer type!");
    }

    /**
     * only bytes and byte<M> are supported at the moment
     */
    private static Class<? extends Type> handleArrayType(JsonObject outerJsonObject) {
        if (outerJsonObject.containsKey("items")) {
            // get the "items" schema, tuples are not yet supported!
            JsonObject jsonObject = outerJsonObject.getJsonObject("items");

            if (jsonObject.containsKey("type") && jsonObject.getString("type").equals("string")) {
                if (jsonObject.containsKey("pattern") && jsonObject.getString("pattern").equals("^[a-fA-F0-9]{2}$")) {
                    if (outerJsonObject.containsKey("maxItems")) {
                        int maxSize = outerJsonObject.getInt("maxItems");
                        if (maxSize > 0 && maxSize <= 32) {
                            return AbiTypes.getType("bytes" + maxSize);
                        }
                    } else {
                        return DynamicBytes.class;
                    }
                }
            }
        }

        throw new ParameterException("Unrecognized array type!");
    }
}
