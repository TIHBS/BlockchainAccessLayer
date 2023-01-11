/*******************************************************************************
 * Copyright (c) 2019-2023 Institute for the Architecture of Application System - University of Stuttgart
 * Author: Ghareeb Falazi
 * Co-author: Akshay Patel
 *
 * This program and the accompanying materials are made available under the
 * terms the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0
 *******************************************************************************/
package blockchains.iaas.uni.stuttgart.de.restapi.model.request;

import org.web3j.crypto.Sign;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "InvokeSmartContractRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class InvokeSmartContractFunctionRequest {
    private String blockchainId;
    private String smartContractPath;
    private String functionIdentifier;
    private double confidence;
    private String subscriptionId;
    private String epUrl;
    private TypeArgumentList typeArguments;
    private ParameterList inputs;
    private ParameterList outputs;
    private long timeoutMillis;
    private String signature;

    private String proposer;

    private SignersList signers;
    private long minimumNumberOfSignatures;

    @XmlElement(name = "Inputs")
    public ParameterList getInputs() {
        return inputs;
    }

    public void setInputs(ParameterList inputs) {
        this.inputs = inputs;
    }

    @XmlElement(name = "SmartContractPath")
    public String getSmartContractPath() {
        return smartContractPath;
    }

    public void setSmartContractPath(String smartContractPath) {
        this.smartContractPath = smartContractPath;
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

    @XmlElement(name = "FunctionIdentifier")
    public String getFunctionIdentifier() {
        return functionIdentifier;
    }

    public void setFunctionIdentifier(String functionIdentifier) {
        this.functionIdentifier = functionIdentifier;
    }

    @XmlElement(name = "BlockchainId")
    public String getBlockchainId() {
        return blockchainId;
    }

    public void setBlockchainId(String blockchainId) {
        this.blockchainId = blockchainId;
    }

    @XmlElement(name = "Outputs")
    public ParameterList getOutputs() {
        return outputs;
    }

    public void setOutputs(ParameterList outputs) {
        this.outputs = outputs;
    }

    @XmlElement(name = "Timeout")
    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    @XmlElement(name = "Signature")
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @XmlElement(name = "signers")
    public SignersList getSigners() {
        return signers;
    }

    public void setSigners(SignersList signers) {
        this.signers = signers;
    }

    @XmlElement(name = "minimumNumberOfSignatures")
    public long getMinimumNumberOfSignatures() {
        return minimumNumberOfSignatures;
    }

    public void setMinimumNumberOfSignatures(long minimumNumberOfSignatures) {
        this.minimumNumberOfSignatures = minimumNumberOfSignatures;
    }

    @XmlElement(name = "typeArguments")
    public TypeArgumentList getTypeArguments() {
        return typeArguments;
    }

    public void setTypeArguments(TypeArgumentList typeArguments) {
        this.typeArguments = typeArguments;
    }

    @XmlElement(name = "signer")
    public String getProposer() {
        return proposer;
    }

    public void setProposer(String proposer) {
        this.proposer = proposer;
    }

}
