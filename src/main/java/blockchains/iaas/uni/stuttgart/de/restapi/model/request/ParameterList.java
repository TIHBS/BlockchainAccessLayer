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
package blockchains.iaas.uni.stuttgart.de.restapi.model.request;

import java.util.List;

import jakarta.xml.bind.annotation.*;

import blockchains.iaas.uni.stuttgart.de.api.model.Parameter;
import lombok.Setter;

// todo rethink type
@Setter
@XmlRootElement(name = "Parameters")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ParameterList {
    private List<Parameter> arguments;

    @XmlElement(name = "Argument")
    public List<Parameter> getArguments() {
        return arguments;
    }

}
