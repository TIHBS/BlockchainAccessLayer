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

package blockchains.iaas.uni.stuttgart.de.externalapi.model.responses;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;


/**
 * Represents the asynchronous result of an INVOKE method.
 */
@Data
@Builder
public class InvocationResponse {
    /**
     * A list of the invocation's returned parameters (variables).
     */
    List<Parameter> params;

    /**
     * A copy of the correlation identifier that was used to issue the INVOKE method.
     */
    @NonNull String correlationIdentifier;

    /**
     * The UTC timestamp of the transaction that invoked the function. If the function did not require a transaction,
     * this field remains empty.
     */
    String timestamp;
}
