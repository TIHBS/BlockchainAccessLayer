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
package blockchains.iaas.uni.stuttgart.de.restapi.model.request;

import jakarta.xml.bind.annotation.*;
import lombok.Setter;

import java.math.BigInteger;


@Setter
@XmlRootElement(name = "SubmitTransactionRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SubmitTransactionRequest {

    private String to;

    private String valueAsString;

    private String blockchainId;

    private String subscriptionId;

    private double requiredConfidence;

    private String epUrl;

    @XmlElement(name="EndPointUrl")
    public String getEpUrl() {
        return epUrl;
    }

    @XmlElement(name="To")
    public String getTo() {
        return to;
    }

    @XmlElement(name="BlockchainId")
    public String getBlockchainId() {
        return blockchainId;
    }

    @XmlElement(name="SubscriptionId")
    public String getSubscriptionId() {
        return subscriptionId;
    }


    @XmlElement(name="RequiredConfidence")
    public double getRequiredConfidence() {
        return requiredConfidence;
    }


    @XmlElement(name="Value")
    public BigInteger getValue(){
        return new BigInteger(valueAsString);
    }

    public void setValue(BigInteger value){
        valueAsString = value.toString();
    }


    public String getValueAsString() {
        return valueAsString;
    }


}
