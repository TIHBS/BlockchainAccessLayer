/*******************************************************************************
 * Copyright (c) 2019-2024 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/

package blockchains.iaas.uni.stuttgart.de.scip.model.responses;

import java.util.List;

import blockchains.iaas.uni.stuttgart.de.scip.model.common.Argument;
import lombok.*;

@Builder
@Setter
@Getter
public class Occurrence {
    @NonNull private String timestamp;
    @NonNull private List<Argument> arguments;

    @Override
    public String toString() {
        return "Occurrence{" +
                "timestamp='" + timestamp + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
