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

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Argument {
    @NonNull private String name;
    @NonNull private String value;

    public static Argument fromParameter(Parameter parameter) {
        return Argument.builder().name(parameter.getName()).value(parameter.getValue()).build();
    }

    @Override
    public String toString() {
        return "Argument{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
