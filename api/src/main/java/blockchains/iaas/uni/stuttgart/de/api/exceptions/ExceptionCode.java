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

public class ExceptionCode {
    public static final int UnknownError = 0;

    /**
     * The blockchain instance, smart contract, event or function are not found
     */
    public static final int NotFound = -32000;
    /**
     * Input parameter types, names, or order mismatch the designated function or event.
     * This also indicates inability to map a parameter's abstract type to a native type.
     */
    public static final int InvalidParameters = -32001;
    /**
     * Client certificate is missing
     */
    public static final int MissingCertificate = -32002;
    /**
     * The client application is not authorized to perform the requested task.
     * Gateway-side authorization.
     */
    public static final int NotAuthorized = -32003;
    /**
     * The specified blockchain instance does not support the requested operation.
     */
    public static final int NotSupported = -32004;
    /**
     * Connection to the underlying blockchain node is not possible.
     */
    public static final int ConnectionException = -32005;
    /**
     * The transaction associated with an function invocation is invalidated after it was mined.
     */
    public static final int TransactionInvalidatedException = -32006;

    /**
     * A scip method parameter has an invalid value
     */
    public static final int InvalidScipParam = -32007;

    /**
     * A general error occurred when trying to invoke a smart contract function
     * This error is used when the specific cause of the error cannot be deteremined.
     */
    public static final int InvocationError = -32100;
    /**
     * The smart contract function threw an exception
     */
    public static final int ExecutionError = -32101;
    /**
     * Not enough funds to invoke the state-changing smart contract funciton.
     */
    public static final int InsufficientFunds = -32102;
    /**
     * The BAL instance is not authorized to performed the requested operation on the underlying blockchain.
     */
    public static final int BalNotAuthorized = -32103;

    /**
     * Timeout is reached before fulfilling the desired degree of confidence.
     * This is an asynchronous error.
     */
    public static final int Timeout = -32201;
}
