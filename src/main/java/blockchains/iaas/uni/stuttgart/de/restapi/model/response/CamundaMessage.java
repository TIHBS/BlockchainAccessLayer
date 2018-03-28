package blockchains.iaas.uni.stuttgart.de.restapi.model.response;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/********************************************************************************
 * Copyright (c) 2018 Institute for the Architecture of Application System -
 * University of Stuttgart
 * Author: Ghareeb Falazi
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
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

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getMessageName() {
        return messageName;
    }

    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public Map<String, CamundaVariable> getProcessVariables() {
        return processVariables;
    }

    public void setProcessVariables(Map<String, CamundaVariable> processVariables) {
        this.processVariables = processVariables;
    }
}
