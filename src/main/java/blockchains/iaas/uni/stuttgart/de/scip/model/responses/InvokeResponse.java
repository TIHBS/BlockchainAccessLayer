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

import blockchains.iaas.uni.stuttgart.de.scip.model.common.Argument;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class InvokeResponse extends AsyncScipResponse {
    private String timeStamp;
    @NonNull private List<Argument> outputArguments;

    @Builder
    public InvokeResponse(@NonNull String correlationId, String timeStamp, @NonNull List<Argument> outputArguments) {
        super(correlationId);
        this.timeStamp = timeStamp;
        this.outputArguments = outputArguments;
    }

    @Override
    public String toString() {
        return "InvokeResponse{" +
                "correlationId='" + getCorrelationId() + '\'' +
                ", timeStamp='" + timeStamp + '\'' +
                ", outputArguments=" + outputArguments +
                '}';
    }
}
