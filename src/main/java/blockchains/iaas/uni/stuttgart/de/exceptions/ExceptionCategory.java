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

package blockchains.iaas.uni.stuttgart.de.exceptions;

import java.util.Arrays;

public enum ExceptionCategory {
    JsonRpcParseError(-32700),
    JsonRpcInvalidRequest(-32600),
    JsonRpcMethodNotFound(-32601),
    JsonRpcInvalidParams(-32602),
    JsonRpcInternalError(-32603),
    NotFound(-32000),
    InvalidInputs(-32001),
    ExecutionError(-32002),
    MissingCertificate(-32003),
    InsufficientFunds(-32004),
    NotAuthorized(-32005),
    Timeout(-32006),
    NotSupported(-32007),
    ConnectionException(-32008),
    UnknownError(0);

    private int code;

    private ExceptionCategory(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ExceptionCategory getExceptionCategoryByCode(int code) {
        return Arrays.stream(ExceptionCategory.values())
                .filter(value -> value.code == code)
                .findFirst()
                .orElse(UnknownError);
    }
}
