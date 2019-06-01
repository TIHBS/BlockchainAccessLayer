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

public class EthereumParameterDecodingException extends Exception {
    public EthereumParameterDecodingException() {
    }

    public EthereumParameterDecodingException(String message) {
        super(message);
    }

    public EthereumParameterDecodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EthereumParameterDecodingException(Throwable cause) {
        super(cause);
    }

    public EthereumParameterDecodingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
