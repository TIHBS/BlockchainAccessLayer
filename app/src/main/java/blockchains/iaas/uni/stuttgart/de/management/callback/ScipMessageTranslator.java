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

package blockchains.iaas.uni.stuttgart.de.management.callback;

import java.util.List;

import blockchains.iaas.uni.stuttgart.de.api.exceptions.BalException;
import blockchains.iaas.uni.stuttgart.de.jsonrpc.model.Occurrence;
import blockchains.iaas.uni.stuttgart.de.jsonrpc.model.ScipResponse;
import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;

public class ScipMessageTranslator {
    public static ScipResponse getInvocationResponseMessage(String correlationId, List<Parameter> outputs) {
        return ScipResponse.builder()
                .correlationIdentifier(correlationId)
                .parameters(outputs)
                .build();
    }

    public static ScipResponse getSubscriptionResponseMessage(String correlationId, List<Parameter> parameters, String isoTimestamp) {
        return ScipResponse.builder()
                .correlationIdentifier(correlationId)
                .parameters(parameters)
                .isoTimestamp(isoTimestamp)
                .build();
    }

    public static ScipResponse getQueryResultResponseMessage(String correlationId, List<Occurrence> occurrences) {
        return ScipResponse.builder()
                .correlationIdentifier(correlationId)
                .occurrences(occurrences)
                .build();
    }

    public static ScipResponse getAsynchronousErrorResponseMessage(String correlationId, BalException exception) {
        return ScipResponse.builder()
                .correlationIdentifier(correlationId)
                .exception(exception)
                .build();
    }
}
