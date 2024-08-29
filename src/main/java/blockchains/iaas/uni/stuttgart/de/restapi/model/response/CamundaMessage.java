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

import java.util.HashMap;
import java.util.Map;


@Setter
@Getter
@XmlRootElement(name="Message")
@XmlAccessorType(XmlAccessType.FIELD)
public class CamundaMessage extends CallbackMessage {
    @XmlElement(name="ProcessInstanceId")
    private String processInstanceId;
    @XmlElement(name="MessageName")
    private String messageName;

    @XmlElementWrapper(name="ProcessVariables")
    @XmlElement(name="ProcessVariable")
    private Map<String, CamundaVariable> processVariables = new HashMap<>();

    @Override
    public String toString() {
        return "CamundaCallbackMessage{" +
                "processInstanceId='" + processInstanceId + '\'' +
                ", messageName='" + messageName + '\'' +
                ", processVariables=" + processVariables +
                '}';
    }
}
