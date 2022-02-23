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

public abstract class AsynchronousBalException extends BalException {
    private String correlationIdentifier;

    public AsynchronousBalException() {
    }

    public AsynchronousBalException(String message) {
        super(message);
    }

    public AsynchronousBalException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCorrelationIdentifier() {
        return correlationIdentifier;
    }

    public void setCorrelationIdentifier(String correlationIdentifier) {
        this.correlationIdentifier = correlationIdentifier;
    }
}
