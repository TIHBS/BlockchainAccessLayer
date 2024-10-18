/*******************************************************************************
 * Copyright (c) 2024 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.scip.model.exceptions;


import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;
import lombok.Getter;

@Getter
public final class AsynchronousBalException extends BalException {
    final private String correlationIdentifier;

    public AsynchronousBalException(BalException cause, String correlationIdentifier) {
        super(cause.getMessage(), cause);
        this.correlationIdentifier = correlationIdentifier;
    }

    @Override
    public int getCode() {
        return ((BalException)getCause()).getCode();
    }

    @Override
    public String toString() {
        return "AsynchronousBalException{" +
                "message='" + getMessage() + '\'' +
                "cause=" + getCause().getClass().getName() +
                "correlationIdentifier='" + correlationIdentifier + '\'' +
                '}';
    }
}