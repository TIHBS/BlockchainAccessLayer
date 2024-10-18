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

import jakarta.xml.bind.annotation.*;
import lombok.Setter;

@Setter
@XmlRootElement(name = "InvokeSmartContractRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class InvokeSmartContractFunctionRequest {
    private String blockchainId;
    private String smartContractPath;
    private String functionIdentifier;
    private double confidence;
    private String subscriptionId;
    private boolean sideEffects;
    private Long nonce;
    private String epUrl;
    private ParameterList inputs;
    private ParameterList outputs;
    private long timeoutMillis;
    private String signature;

    @XmlElement(name = "Inputs")
    public ParameterList getInputs() {
        return inputs;
    }

    @XmlElement(name = "SmartContractPath")
    public String getSmartContractPath() {
        return smartContractPath;
    }

    @XmlElement(name = "Confidence")
    public double getConfidence() {
        return confidence;
    }

    @XmlElement(name = "SubscriptionId")
    public String getSubscriptionId() {
        return subscriptionId;
    }

    @XmlElement(name = "EndPointUrl")
    public String getEpUrl() {
        return epUrl;
    }

    @XmlElement(name = "FunctionIdentifier")
    public String getFunctionIdentifier() {
        return functionIdentifier;
    }

    @XmlElement(name = "BlockchainId")
    public String getBlockchainId() {
        return blockchainId;
    }

    @XmlElement(name = "Outputs")
    public ParameterList getOutputs() {
        return outputs;
    }

    @XmlElement(name = "Timeout")
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    @XmlElement(name = "Signature")
    public String getSignature() {
        return signature;
    }

    @XmlElement(name = "SideEffects")
    public boolean isSideEffects() {
        return sideEffects;
    }

    @XmlElement(name = "Nonce")
    public Long getNonce() {
        return nonce;
    }
}
