/********************************************************************************
 * Copyright (c) 2018-2024 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.model.response;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@XmlRootElement(name="Variable")
@XmlAccessorType(XmlAccessType.FIELD)
public class CamundaVariable {
    @XmlElement(name="Value")
    private String value;
    @XmlElement(name="Type")
    private String type;

    public CamundaVariable(){

    }

    public CamundaVariable(String value, String type) {
        this.value = value;
        this.type = type;
    }

}
