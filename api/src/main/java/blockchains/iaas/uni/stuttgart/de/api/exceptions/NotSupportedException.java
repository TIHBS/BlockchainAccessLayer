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

package blockchains.iaas.uni.stuttgart.de.api.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;

@JsonRpcError(code = ExceptionCode.NotSupported, message = "The requested operation is not supported by the underlying blockchain instance.")
public class NotSupportedException extends BalException {
    public NotSupportedException() {
    }

    public NotSupportedException(String message) {
        super(message);
    }

    @Override
    public int getCode() {
        return ExceptionCode.NotSupported;
    }
}
