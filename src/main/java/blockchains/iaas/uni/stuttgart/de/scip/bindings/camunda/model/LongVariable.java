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

package blockchains.iaas.uni.stuttgart.de.scip.bindings.camunda.model;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LongVariable extends Variable {
    private final long value;

    public LongVariable(long value) {
        super("Long");
        this.value = value;
    }
}