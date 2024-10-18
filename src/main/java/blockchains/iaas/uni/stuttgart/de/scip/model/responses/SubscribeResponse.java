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

@Builder
@Setter
@Getter
public class SubscribeResponse {
    @NonNull private String correlationId;
    @NonNull private String timestamp;
    @NonNull private List<Argument> arguments;

    @Override
    public String toString() {
        return "SubscribeResponse{" +
                "correlationId='" + correlationId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
