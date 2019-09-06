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

public class InvokeSmartContractFunctionFailure extends RuntimeException {
    public InvokeSmartContractFunctionFailure() {
    }

    public InvokeSmartContractFunctionFailure(String message) {
        super(message);
    }

    public InvokeSmartContractFunctionFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokeSmartContractFunctionFailure(Throwable cause) {
        super(cause);
    }

    public InvokeSmartContractFunctionFailure(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
