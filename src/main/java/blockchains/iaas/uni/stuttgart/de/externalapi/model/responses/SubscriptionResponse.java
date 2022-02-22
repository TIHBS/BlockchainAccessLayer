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

import lombok.Data;

/**
 * One of the asynchronous response messages of a SUBSCRIBE method. Every instance of this class represents a single
 * occurrence of the event or smart contract function invocation that is being monitored by the SUBSCRIBE method.
 */
@Data
public class SubscriptionResponse {
    /**
     * A list of parameters associated with the reported occurrence.
     */
    List<Parameter> params;

    /**
     * A copy of the correlation identifier that was used to issue the SUBSCRIBE method.
     */
    String correlationIdentifier;

    /**
     * The UTC timestamp of the transaction associated with the occurrence.
     */
    String timestamp;
}
