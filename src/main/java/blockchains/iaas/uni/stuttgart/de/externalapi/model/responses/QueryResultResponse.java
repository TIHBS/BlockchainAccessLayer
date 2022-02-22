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
 * A synchronous response message for the QUERY method. Represents the set of all event or smart contract function
 * invocation occurrences that match with the QUERY.
 */
@Data
public class QueryResultResponse {
    List<Occurrence> occurrences;
}
