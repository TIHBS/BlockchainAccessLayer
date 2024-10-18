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

package blockchains.iaas.uni.stuttgart.de.scip.model.responses;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Builder
@Setter
@Getter
public class InvokeResponse {
    @NonNull private String correlationId;
    private String timeStamp;
    @NonNull private List<Argument> outputArguments;

    @Override
    public String toString() {
        return "InvokeResponse{" +
                "correlationId='" + correlationId + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", outputArguments=" + outputArguments +
                '}';
    }
}
