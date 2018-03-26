package blockchains.iaas.uni.stuttgart.de.restapi.model.response;

import blockchains.iaas.uni.stuttgart.de.restapi.model.ResourceSupport;

import javax.xml.bind.annotation.*;

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
@XmlRootElement(name="CorrelatedResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CorrelatedResponse extends ResourceSupport {
    @XmlAttribute(name="correlationId")
    private String correlationId;

    CorrelatedResponse(){

    }

    CorrelatedResponse(final String correlationId){
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

}
