/********************************************************************************
 * Copyright (c) 2018-2019 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.api.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;

@JsonRpcError(code = ExceptionCode.TransactionInvalidatedException, message = "The transaction associated with an function invocation is invalidated after it was mined.")
public class TransactionNotFoundException extends BalException {

    public TransactionNotFoundException() {
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        return ExceptionCode.TransactionInvalidatedException;
    }
}
