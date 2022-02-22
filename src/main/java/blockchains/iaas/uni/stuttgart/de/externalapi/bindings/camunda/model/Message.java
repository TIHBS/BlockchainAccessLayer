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

package blockchains.iaas.uni.stuttgart.de.externalapi.bindings.camunda.model;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class Message {
    @NonNull
    private String processInstanceId;
    @NonNull
    private String messageName;
    @NonNull
    private Map<String, Variable> processVariables;
}
