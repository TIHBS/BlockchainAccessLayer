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

package blockchains.iaas.uni.stuttgart.de.externalapi.model.exceptions;

import com.github.arteam.simplejsonrpc.core.annotation.JsonRpcError;
import lombok.Getter;
import lombok.Setter;

@JsonRpcError(code = ExceptionCode.Timeout, message = "Timeout was reached before the desired DoC was fulfilled.")
@Setter
@Getter
public class TimeoutException extends AsynchronousBalException {
    private String transactionHash;
    private double doc;

    public TimeoutException() {
    }

    public TimeoutException(String message, String transactionHash, double doc) {
        super(message);
        this.transactionHash = transactionHash;
        this.doc = doc;
    }

    @Override
    public int getCode() {
        return ExceptionCode.Timeout;
    }
}
