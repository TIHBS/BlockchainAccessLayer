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
public class SubscribeResponse extends AsyncScipResponse {
    @NonNull private String timestamp;
    @NonNull private List<Argument> arguments;

    @Builder
    public SubscribeResponse(@NonNull String correlationId, @NonNull String timestamp, @NonNull List<Argument> arguments) {
        super(correlationId);
        this.timestamp = timestamp;
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "SubscribeResponse{" +
                "correlationId='" + getCorrelationId() + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
