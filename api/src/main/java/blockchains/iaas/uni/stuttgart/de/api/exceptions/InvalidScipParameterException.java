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

@JsonRpcError(code = ExceptionCode.InvalidScipParam, message = "A scip method parameter has an invalid value.")
public class InvalidScipParameterException extends BalException {
    @Override
    public int getCode() {
        return ExceptionCode.InvalidScipParam;
    }

    public InvalidScipParameterException() {
    }

    public InvalidScipParameterException(String message) {
        super(message);
    }
}
