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
 * A single occurrence of an event or a smart contract function invocation.
 */
@Data
public class Occurrence {
    /**
     * A list of parameters associated with the reported occurrence.
     */
    List<Parameter> params;

    /**
     * The UTC timestamp of the transaction associated with the occurrence.
     */
    String timestamp;
}
