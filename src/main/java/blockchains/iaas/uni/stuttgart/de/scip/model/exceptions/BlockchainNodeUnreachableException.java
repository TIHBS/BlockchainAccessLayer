/*******************************************************************************
 * Copyright (c) 2022 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.scip.model.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;

@JsonRpcError(code = ExceptionCode.ConnectionException, message = "The blockchain node cannot be reached.")
public class BlockchainNodeUnreachableException extends BalException {

    public BlockchainNodeUnreachableException() {
    }

    public BlockchainNodeUnreachableException(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        return ExceptionCode.ConnectionException;
    }
}
