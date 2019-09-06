/*******************************************************************************
 * Copyright (c) 2019 Institute for the Architecture of Application System - University of Stuttgart
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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import blockchains.iaas.uni.stuttgart.de.model.SmartContractFunctionArgument;

@XmlRootElement(name = "InvokeSmartContractRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class InvokeSmartContractFunctionRequest {
    private String scip;
    private double confidence;
    private String subscriptionId;
    private String epUrl;
    private ParameterList parameterList;

    @XmlElement(name = "Parameters")
    public ParameterList getParameterList() {
        return parameterList;
    }

    public void setParameterList(ParameterList parameterList) {
        this.parameterList = parameterList;
    }

    @XmlElement(name = "Function")
    public String getScip() {
        return scip;
    }

    public void setScip(String scip) {
        this.scip = scip;
    }

    @XmlElement(name = "Confidence")
    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @XmlElement(name = "SubscriptionId")
    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @XmlElement(name = "EndPointUrl")
    public String getEpUrl() {
        return epUrl;
    }

    public void setEpUrl(String epUrl) {
        this.epUrl = epUrl;
    }
}
