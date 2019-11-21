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
package blockchains.iaas.uni.stuttgart.de.exceptions;

public class ManualUnsubscriptionException extends RuntimeException {
    public ManualUnsubscriptionException() {
        super();
    }

    public ManualUnsubscriptionException(String message) {
        super(message);
    }

    public ManualUnsubscriptionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManualUnsubscriptionException(Throwable cause) {
        super(cause);
    }

    protected ManualUnsubscriptionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
