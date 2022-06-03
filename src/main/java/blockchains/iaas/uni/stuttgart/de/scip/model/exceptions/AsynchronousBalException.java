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

import blockchains.iaas.uni.stuttgart.de.exceptions.BalException;

public final class AsynchronousBalException extends BalException {
    final private String correlationIdentifier;

    public AsynchronousBalException(BalException cause, String correlationIdentifier) {
        super(cause.getMessage(), cause);
        this.correlationIdentifier = correlationIdentifier;
    }

    public String getCorrelationIdentifier() {
        return correlationIdentifier;
    }

    @Override
    public int getCode() {
        return ((BalException)getCause()).getCode();
    }
}
