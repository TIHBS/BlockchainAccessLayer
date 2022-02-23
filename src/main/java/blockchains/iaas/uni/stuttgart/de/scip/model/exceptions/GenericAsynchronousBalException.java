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

public class GenericAsynchronousBalException extends AsynchronousBalException {

    public GenericAsynchronousBalException(BalException exception, String correlationIdentifier) {
        super(exception.getMessage(), exception);
        setCorrelationIdentifier(correlationIdentifier);
    }

    @Override
    public int getCode() {
        return ((BalException)getCause()).getCode();
    }
}
