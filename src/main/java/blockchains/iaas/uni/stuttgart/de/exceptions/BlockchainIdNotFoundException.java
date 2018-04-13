package blockchains.iaas.uni.stuttgart.de.exceptions;

/********************************************************************************
 * Copyright (c) 2018 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
public class BlockchainIdNotFoundException extends RuntimeException {
    public BlockchainIdNotFoundException() {
        super();
    }

    public BlockchainIdNotFoundException(String message) {
        super(message);
    }

    public BlockchainIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockchainIdNotFoundException(Throwable cause) {
        super(cause);
    }

    protected BlockchainIdNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
