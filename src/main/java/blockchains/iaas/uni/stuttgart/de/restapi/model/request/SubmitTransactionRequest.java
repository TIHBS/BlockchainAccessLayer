package blockchains.iaas.uni.stuttgart.de.restapi.model.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigInteger;

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
@XmlRootElement(name = "SubmitTransactionRequest")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class SubmitTransactionRequest {

    private String to;

    private String valueAsString;

    private String blockchainId;

    private String subscriptionId;

    private long waitFor;

    private String epUrl;

    @XmlElement(name="EndPointUrl")
    public String getEpUrl() {
        return epUrl;
    }

    public void setEpUrl(String epUrl) {
        this.epUrl = epUrl;
    }

    @XmlElement(name="To")
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @XmlElement(name="BlockchainId")
    public String getBlockchainId() {
        return blockchainId;
    }

    public void setBlockchainId(String blockchainId) {
        this.blockchainId = blockchainId;
    }

    @XmlElement(name="SubscriptionId")
    public String getSubscriptionId() {
        return subscriptionId;
    }


    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    @XmlElement(name="WaitFor")
    public long getWaitFor() {
        return waitFor;
    }


    public void setWaitFor(long waitFor) {
        this.waitFor = waitFor;
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

    public void setValueAsString(String value) {
        this.valueAsString = value;
    }


}
